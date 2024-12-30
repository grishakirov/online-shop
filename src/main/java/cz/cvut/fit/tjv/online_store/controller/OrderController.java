package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved orders")
    })
    @GetMapping
    public List<OrderDto> getAllOrders() {
        return (List<OrderDto>) orderService.findAll();
    }

    @Operation(summary = "Get order by ID", description = "Retrieve details of a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id:[0-9]+}")
    public OrderDto getOrderById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @Operation(summary = "Create a new order", description = "Create a new order for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderService.save(orderDto);
    }

    @Operation(summary = "Delete an order", description = "Delete an existing order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
    }

    @Operation(summary = "Update order status", description = "Update the status of an existing order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PatchMapping("/{id:[0-9]+}/status")
    public OrderDto updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request body cannot be null or empty.");
        }

        System.out.println("Request body: " + request);
        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("The 'status' field is required.");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return orderService.updateStatus(id, orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    @Operation(summary = "Add products to an existing order in DRAFT status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully added to the order"),
            @ApiResponse(responseCode = "400", description = "Invalid input or order not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id:[0-9]+}/add-products")
    public OrderDto addProductsToOrder(@PathVariable Long id, @RequestBody Map<Long, Integer> productsToAdd) {
        if (productsToAdd == null || productsToAdd.isEmpty()) {
            throw new IllegalArgumentException("Product quantities to add are required.");
        }
        System.out.println(productsToAdd);
        OrderDto existingOrder = orderService.findById(id);
        if (existingOrder.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot add products to an order that is not in DRAFT status.");
        }

        Map<Long, Integer> updatedQuantities = new HashMap<>(existingOrder.getRequestedQuantities());
        productsToAdd.forEach((productId, quantityToAdd) -> {
            if (quantityToAdd <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than zero for product ID: " + productId);
            }
            updatedQuantities.merge(productId, quantityToAdd, Integer::sum);
        });

        existingOrder.setRequestedQuantities(updatedQuantities);
        return orderService.save(existingOrder);
    }

    @Operation(summary = "Get current user's draft order", description = "Retrieve the draft order of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No draft order found")
    })
    @GetMapping("/my/draft")
    public OrderDto getMyDraftOrder(Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderService.findUserDraftOrder(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No draft order found"));
    }

    @Operation(summary = "Ensure a draft order exists for the authenticated user",
            description = "Retrieve the current user's draft order. If no draft order exists, a new one will be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft order retrieved or created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Error occurred while processing the request")
    })
    @GetMapping("/my/draft/ensure")
    public OrderDto ensureDraftOrderExists(Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderService.findUserDraftOrder(user.getId())
                .orElseGet(() -> {
                    OrderDto newDraftOrder = new OrderDto();
                    newDraftOrder.setUserId(user.getId());
                    newDraftOrder.setStatus(OrderStatus.DRAFT);
                    newDraftOrder.setRequestedQuantities(new HashMap<>());
                    return orderService.save(newDraftOrder);
                });
    }
    @Operation(summary = "Get all orders of the current user",
            description = "Retrieve a list of all orders associated with the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's orders")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderDto> getMyOrders(Authentication authentication) {

        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException( "User not found"));

        return orderService.findAllByUserId(user.getId());
    }

    @Operation(summary = "Add products to cart", description = "Add specified products to the user's cart. If a draft order exists, add to it; otherwise, create a new draft order and add the products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully added to the cart"),
            @ApiResponse(responseCode = "400", description = "Invalid input or unable to add products"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/add-to-cart")
    public OrderDto addToCart(Authentication authentication, @RequestBody Map<String, Integer> productsToAdd) {
        if (productsToAdd == null || productsToAdd.isEmpty()) {
            throw new IllegalArgumentException("Product quantities to add are required.");
        }

        Map<Long, Integer> productsToAddLong = new HashMap<>();
        for (Map.Entry<String, Integer> entry : productsToAdd.entrySet()) {
            try {
                Long productId = Long.parseLong(entry.getKey());
                Integer quantity = entry.getValue();
                productsToAddLong.put(productId, quantity);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid product ID format: " + entry.getKey());
            }
        }

        String userEmail = authentication.getName();
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        OrderDto draftOrder = orderService.getOrCreateDraftOrder(user.getId());
        return orderService.addProductsToOrder(draftOrder.getId(), productsToAddLong);
    }

    @Operation(summary = "Finalize the order", description = "Finalize the order by updating its status to PROCESSING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or order not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{id:[0-9]+}/confirm")
    public OrderDto confirmOrder(@PathVariable long id) {

        return orderService.updateStatus(id, OrderStatus.PROCESSING);
    }

    @Operation(summary = "Get the last order of the authenticated user", description = "Retrieve the most recent order placed by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No orders found for the user")
    })
    @GetMapping("/last")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderDto getLastOrder(Authentication authentication) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderService.findLastOrderByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No orders found for the user"));
    }

    @DeleteMapping("/{orderId}/products/{productId}")
    public OrderDto deleteProductFromCart(@PathVariable Long orderId, @PathVariable Long productId) {
        return orderService.deleteProductFromCart(orderId, productId);
    }
}
