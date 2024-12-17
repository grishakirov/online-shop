package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get all orders")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders")})
    @GetMapping
    public List<OrderDto> getAllOrders() {
        return (List<OrderDto>) orderService.findAll();
    }

    @Operation(summary = "Get order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public OrderDto getOrderById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @Operation(summary = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created, bonus card logic applied if applicable"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing required fields, invalid references, invalid bonus card usage)"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderService.save(orderDto);
    }

    @Operation(summary = "Delete an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., malformed ID)")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
    }

    @Operation(summary = "Update the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input: missing or invalid 'status' field"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        if (statusUpdate == null || !statusUpdate.containsKey("status")) {
            throw new IllegalArgumentException("The 'status' field is required.");
        }
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(statusUpdate.get("status"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + statusUpdate.get("status"));
        }
        return ResponseEntity.ok(orderService.updateStatus(id, orderStatus));
    }

    @Operation(summary = "Add products to an existing order when status is DRAFT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully added to the order"),
            @ApiResponse(responseCode = "400", description = "Invalid input or order not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/add-products")
    public ResponseEntity<OrderDto> addProductsToOrder(@PathVariable Long id, @RequestBody Map<Long, Integer> productsToAdd) {
        if (productsToAdd == null || productsToAdd.isEmpty()) {
            throw new IllegalArgumentException("Product quantities to add are required.");
        }
        OrderDto existingOrder = orderService.findById(id);
        if (existingOrder.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot add products to an order that is not in DRAFT status.");
        }
        Map<Long, Integer> updatedQuantities = existingOrder.getRequestedQuantities();
        productsToAdd.forEach((productId, quantityToAdd) -> {
            if (quantityToAdd <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than zero for product ID: " + productId);
            }
            updatedQuantities.merge(productId, quantityToAdd, Integer::sum);
        });
        existingOrder.setRequestedQuantities(updatedQuantities);
        OrderDto updatedOrder = orderService.save(existingOrder);

        return ResponseEntity.ok(updatedOrder);
    }
}