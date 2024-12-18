package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BonusCardRepository bonusCardRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final BonusCardService bonusCardService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository, BonusCardRepository bonusCardRepository,
                        ProductRepository productRepository,
                        OrderMapper orderMapper,
                        BonusCardService bonusCardService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bonusCardRepository = bonusCardRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.bonusCardService = bonusCardService;
    }

    private boolean isValidStatus(String status) {
        try {
            OrderStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public OrderDto save(OrderDto orderDto) {

        validateOrderDto(orderDto);

        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        fetchAndValidateProducts(orderDto, user);

        Order existingOrder = (orderDto.getId() != null) ? orderRepository.findById(orderDto.getId()).orElse(null) : null;
        Order order;

        if (existingOrder != null && existingOrder.getStatus() == OrderStatus.DRAFT) {
            mergeRequestedQuantities(existingOrder, orderDto.getRequestedQuantities());
            order = existingOrder;
        } else {
            order = Optional.ofNullable(orderMapper.convertToEntity(orderDto))
                    .orElseThrow(() -> new IllegalStateException("Failed to map OrderDto to Order"));
            order.setUser(user);
            order.setRequestedQuantities(orderDto.getRequestedQuantities());
            order.setDateOfCreation(LocalDate.now());
            order.setStatus(OrderStatus.PROCESSING);
        }

        updateProductStock(orderDto);

        double remainingCost = calculateTotalCost(order);

        BonusCard bonusCard = bonusCardRepository.findByUserId(user.getId()).orElse(null);
        if (bonusCard != null && bonusCard.getBalance() > 0) {
            double amountToDeduct = Math.min(bonusCard.getBalance(), remainingCost);
            bonusCardService.deductBalance(bonusCard.getId(), amountToDeduct);
            remainingCost -= amountToDeduct;
        }

        order.setTotalCost(remainingCost);
        Order savedOrder = orderRepository.save(order);

        if (order.getStatus() == OrderStatus.PROCESSING && bonusCard != null) {
            double cashback = remainingCost * 0.05;
            bonusCardService.addBalance(bonusCard.getId(), cashback);
        }

        return orderMapper.convertToDto(savedOrder);
    }

    private void mergeRequestedQuantities(Order existingOrder, Map<Long, Integer> newQuantities) {
        Map<Long, Integer> existingQuantities = existingOrder.getRequestedQuantities();

        newQuantities.forEach((productId, newQuantity) -> {
            if (newQuantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + productId);
            }
            existingQuantities.put(productId, newQuantity);
        });

        existingOrder.setRequestedQuantities(existingQuantities);
    }

    private void updateProductStock(OrderDto orderDto) {
        orderDto.getRequestedQuantities().forEach((productId, quantity) -> {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

            if (product.getQuantity() < quantity) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
        });
    }

    private double calculateTotalCost(Order order) {
        return order.getRequestedQuantities().entrySet().stream()
                .mapToDouble(entry -> {
                    Long productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
                    return product.getPrice() * quantity;
                })
                .sum();
    }

    private void validateOrderDto(OrderDto orderDto) {
        if (orderDto.getRequestedQuantities() == null
                || orderDto.getRequestedQuantities().isEmpty()
                || orderDto.getRequestedQuantities().values().stream().anyMatch(quantity -> quantity <= 0)) {
            throw new IllegalArgumentException("Requested quantities cannot be null, empty, or contain non-positive values.");
        }

        if (orderDto.getTotalCost() <= 0) {
            throw new IllegalArgumentException("Total cost must be greater than zero.");
        }
        if (orderDto.getStatus() == null) {
            orderDto.setStatus(OrderStatus.DRAFT);
        }
        if (!isValidStatus(orderDto.getStatus().name())) {
            throw new IllegalArgumentException("Invalid order status: " + orderDto.getStatus());
        }
    }

    private void fetchAndValidateProducts(OrderDto orderDto, User user) {
        List<Long> productIds = new ArrayList<>(orderDto.getRequestedQuantities().keySet());
        List<Product> products = StreamSupport.stream(productRepository.findAllById(productIds).spliterator(), false)
                .toList();

        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products were not found in the database");
        }

        int userAge = (user.getBirthDate() != null)
                ? Period.between(user.getBirthDate(), LocalDate.now()).getYears()
                : 0;
        for (Product product : products) {
            if (product.getAllowedAge() != null && (userAge < product.getAllowedAge())) {
                throw new IllegalStateException("User is too young to purchase product: " + product.getName());
            }
            if (product.getQuantity() < orderDto.getRequestedQuantities().get(product.getId())) {
                throw new IllegalArgumentException("Product " + product.getName() + " is out of stock.");
            }
        }
    }


    public Iterable<OrderDto> findAll() {
        List<Order> orders = StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        return orderMapper.converManyToDto(orders);
    }

    public OrderDto findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return orderMapper.convertToDto(order);
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    public OrderDto updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.convertToDto(updatedOrder);
    }

    public Optional<OrderDto> findUserDraftOrder(Long userId) {
        return orderRepository.findByUserIdAndStatus(userId, OrderStatus.DRAFT)
                .map(orderMapper::convertToDto);
    }
}