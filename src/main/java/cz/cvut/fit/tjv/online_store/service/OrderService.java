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
import java.util.ArrayList;
import java.util.List;
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
        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        validateOrderDto(orderDto);
        List<Product> products = fetchAndValidateProducts(orderDto, user);

        double remainingCost = orderDto.getTotalCost();

        BonusCard bonusCard = bonusCardRepository.findByUserId(user.getId()).orElse(null);

        if (bonusCard != null && bonusCard.getBalance() > 0) {
            double amountToDeduct = Math.min(bonusCard.getBalance(), remainingCost);
            bonusCardService.deductBalance(bonusCard.getId(), amountToDeduct);
            remainingCost -= amountToDeduct;
        }

        Order order = orderMapper.convertToEntity(orderDto);
        order.setUser(user);
        order.setProducts(products);
        order.setTotalCost(remainingCost);
        order.setDateOfCreation(LocalDate.now());
        Order savedOrder = orderRepository.save(order);

        if (bonusCard != null) {
            double cashback = remainingCost * 0.05;
            bonusCardService.addBalance(bonusCard.getId(), cashback);
        }

        return orderMapper.convertToDto(savedOrder);
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
            throw new IllegalArgumentException("Order status cannot be null.");
        }
        if (!isValidStatus(orderDto.getStatus().name())) {
            throw new IllegalArgumentException("Invalid order status: " + orderDto.getStatus());
        }
    }

    private List<Product> fetchAndValidateProducts(OrderDto orderDto, User user) {
        List<Long> productIds = new ArrayList<>(orderDto.getRequestedQuantities().keySet());
        List<Product> products = StreamSupport.stream(productRepository.findAllById(productIds).spliterator(), false)
                .collect(Collectors.toList());

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
        return products;
    }



    public OrderDto createOrder(OrderDto orderDto) {
        if (orderDto.getRequestedQuantities() == null || orderDto.getRequestedQuantities().isEmpty()) {
            throw new IllegalArgumentException("Requested quantities cannot be null or empty.");
        }
        if (orderDto.getRequestedQuantities().values().stream().anyMatch(q -> q <= 0)) {
            throw new IllegalArgumentException("Requested quantities must be positive.");
        }
        if (orderDto.getTotalCost() <= 0) {
            throw new IllegalArgumentException("Total cost must be greater than zero.");
        }
        if (orderDto.getStatus() == null || !isValidStatus(orderDto.getStatus().name())) {
            throw new IllegalArgumentException("Invalid order status: " + orderDto.getStatus());
        }

        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + orderDto.getUserId()));

        if (user.getBirthDate() == null) {
            throw new IllegalStateException("User's birth date is required for age-restricted products!");
        }
        List<Long> productIds = new ArrayList<>(orderDto.getRequestedQuantities().keySet());
        List<Product> products = (List<Product>) productRepository.findAllById(productIds);
        List<Product> updatedProducts = new ArrayList<>();

        int userAge = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        double totalCost = 0.0;

        for (Product product : products) {
            if (product.getAllowedAge() != null && userAge < product.getAllowedAge()) {
                throw new IllegalStateException("User is too young to purchase product: " + product.getName());
            }

            int requestedQuantity = orderDto.getRequestedQuantities().get(product.getId());
            if (requestedQuantity > product.getQuantity()) {
                requestedQuantity = product.getQuantity();
            }

            if (requestedQuantity == 0) {
                throw new IllegalStateException("Product " + product.getName() + " is out of stock!");
            }

            product.setQuantity(product.getQuantity() - requestedQuantity);
            updatedProducts.add(product);
            totalCost += requestedQuantity * product.getPrice();
        }

        productRepository.saveAll(updatedProducts);

        Order order = new Order();
        order.setUser(user);
        order.setProducts(products);
        order.setDateOfCreation(LocalDate.now());
        order.setTotalCost(totalCost);
        order.setStatus(OrderStatus.PROCESSING);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.convertToDto(savedOrder);
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
}