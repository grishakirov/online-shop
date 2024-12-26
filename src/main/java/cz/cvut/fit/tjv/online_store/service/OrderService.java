package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            BonusCardRepository bonusCardRepository,
            ProductRepository productRepository,
            OrderMapper orderMapper,
            BonusCardService bonusCardService
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bonusCardRepository = bonusCardRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.bonusCardService = bonusCardService;
    }


    public OrderDto save(OrderDto orderDto) {
        validateOrderDto(orderDto);

        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with ID: " + orderDto.getUserId()));
        Order existingOrder = (orderDto.getId() != null)
                ? orderRepository.findById(orderDto.getId()).orElse(null)
                : null;

        Order order;

        if (existingOrder != null && existingOrder.getStatus() == OrderStatus.DRAFT) {
            mergeRequestedQuantities(existingOrder, orderDto.getRequestedQuantities());
            order = existingOrder;
        } else {
            order = orderMapper.convertToEntity(orderDto);
            if (order == null) {
                throw new IllegalStateException("Failed to map OrderDto to Order.");
            }
            order.setUser(user);
            order.setStatus(OrderStatus.DRAFT);
            order.setDateOfCreation(LocalDate.now());
            order.setRequestedQuantities(
                    (orderDto.getRequestedQuantities() != null)
                            ? orderDto.getRequestedQuantities()
                            : new HashMap<>()
            );
        }
        System.out.println("whyyyy");
        checkAgeRestrictions(order);
        double hypotheticalCost = calculateTotalCost(order);
        order.setTotalCost(hypotheticalCost);

        Order saved = orderRepository.save(order);
        return orderMapper.convertToDto(saved);
    }



    public OrderDto updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with ID: " + orderId));

        if (newStatus == OrderStatus.DRAFT && order.getStatus() != OrderStatus.DRAFT) {
            boolean alreadyHasDraft = orderRepository.existsByUserIdAndStatusIn(
                    order.getUser().getId(),
                    List.of(OrderStatus.DRAFT)
            );
            if (alreadyHasDraft) {
                List<Order> allDrafts = orderRepository.findByUserIdAndStatusIn(
                        order.getUser().getId(),
                        List.of(OrderStatus.DRAFT)
                );
                boolean hasAnotherDraft = allDrafts.stream()
                        .anyMatch(d -> !d.getId().equals(orderId));
                if (hasAnotherDraft) {
                    throw new IllegalStateException(
                            "User already has another DRAFT order. Cannot have two DRAFT orders at once."
                    );
                }
            }
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (oldStatus != OrderStatus.PROCESSING && newStatus == OrderStatus.PROCESSING) {
            checkAgeRestrictions(order);
            List<String> clampWarnings = clampRequestedQuantities(order);
            subtractStockForFinalConfirm(order);
            double finalCost = calculateTotalCost(order);
            order.setTotalCost(finalCost);
            BonusCard bonusCard = bonusCardRepository.findByUserId(order.getUser().getId())
                    .orElse(null);
            if (bonusCard != null && finalCost > 0.0) {
                handleBonusUsage(order, bonusCard);
                double cashback = order.getTotalCost() * 0.05;
                bonusCardService.addBalance(bonusCard.getId(), cashback);
            }

            if (!clampWarnings.isEmpty()) {
               throw new IllegalArgumentException("Warnings: " + clampWarnings);
            }
        }

        Order saved = orderRepository.save(order);
        return orderMapper.convertToDto(saved);
    }

    private void checkAgeRestrictions(Order order) {
        LocalDate birthDate = order.getUser().getBirthDate();
        int userAge = 0;
        if (birthDate != null) {
            userAge = Period.between(birthDate, LocalDate.now()).getYears();
        }

        for (Map.Entry<Long, Integer> e : order.getRequestedQuantities().entrySet()) {
            Product p = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found with ID: " + e.getKey()));

            if (p.getAllowedAge() != null && userAge < p.getAllowedAge()) {
                throw new IllegalArgumentException(
                        "User is too young to buy " + p.getName());
            }
        }
    }

    private List<String> clampRequestedQuantities(Order order) {
        List<String> clampWarnings = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : order.getRequestedQuantities().entrySet()) {
            Product p = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found with ID: " + e.getKey()));
            int requested = e.getValue();
            if (requested > p.getQuantity()) {
                clampWarnings.add("Clamped product '" + p.getName()
                        + "' from " + requested
                        + " to " + p.getQuantity());
                order.getRequestedQuantities().put(e.getKey(), p.getQuantity());
            }
        }
        return clampWarnings;
    }

    private void subtractStockForFinalConfirm(Order order) {
        for (Map.Entry<Long, Integer> e : order.getRequestedQuantities().entrySet()) {
            int qty = e.getValue();
            if (qty <= 0) continue;

            Product p = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found with ID: " + e.getKey()));
            p.setQuantity(p.getQuantity() - qty);
            productRepository.save(p);
        }
    }

    private void handleBonusUsage(Order order, BonusCard bonusCard) {

        double finalCost = (order.getTotalCost() != null) ? order.getTotalCost() : 0.0;
        double previouslyUsed = (order.getBonusPointsUsed() != null) ? order.getBonusPointsUsed() : 0.0;
        double balance = bonusCard.getBalance();
        if (finalCost <= 0) {
            if (previouslyUsed > 0) {
                bonusCardService.addBalance(bonusCard.getId(), previouslyUsed);
                order.setBonusPointsUsed(0.0);
            }
            return;
        }
        double desiredUsage = Math.min(finalCost, balance);
        double difference = desiredUsage - previouslyUsed;

        if (difference > 0) {
            double toDeduct = Math.min(balance, difference);
            if (toDeduct > 0) {
                bonusCardService.deductBalance(bonusCard.getId(), toDeduct);
                order.setBonusPointsUsed(previouslyUsed + toDeduct);
                double newCost = finalCost - toDeduct;
                order.setTotalCost(Math.max(newCost, 0.0));
            }
        }
        else if (difference < 0) {
            double toReturn = Math.min(previouslyUsed, Math.abs(difference));
            if (toReturn > 0) {
                bonusCardService.addBalance(bonusCard.getId(), toReturn);
                double newUsed = previouslyUsed - toReturn;
                order.setBonusPointsUsed(Math.max(0, newUsed));
                double newCost = finalCost + toReturn;
                order.setTotalCost(newCost);
            }
        }
        System.out.println(order.getTotalCost());
    }

    private void mergeRequestedQuantities(Order existingOrder, Map<Long, Integer> newQuantities) {
        if (newQuantities == null) return;
        Map<Long, Integer> existing = existingOrder.getRequestedQuantities();
        if (existing == null) existing = new HashMap<>();

        for (Map.Entry<Long, Integer> e : newQuantities.entrySet()) {
            if (e.getValue() <= 0) {
                existing.remove(e.getKey());
            } else {
                existing.put(e.getKey(), e.getValue());
            }
        }
        existingOrder.setRequestedQuantities(existing);
    }

    private double calculateTotalCost(Order order) {
        return order.getRequestedQuantities().entrySet().stream()
                .mapToDouble(e -> {
                    Product product = productRepository.findById(e.getKey())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product not found with ID: " + e.getKey()));
                    return product.getPrice() * e.getValue();
                })
                .sum();
    }

    private void validateOrderDto(OrderDto dto) {
        if (dto.getStatus() == null) {
            dto.setStatus(OrderStatus.DRAFT);
        }
    }


    public Iterable<OrderDto> findAll() {
        List<Order> orders = StreamSupport
                .stream(orderRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        return orderMapper.convertManyToDto(orders);
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

    public Optional<OrderDto> findUserDraftOrder(Long userId) {
        return orderRepository.findByUserIdAndStatus(userId, OrderStatus.DRAFT)
                .map(orderMapper::convertToDto);
    }

    public List<OrderDto> findAllByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orderMapper.convertManyToDto(orders);
    }

    @Transactional
    public OrderDto getOrCreateDraftOrder(Long userId) {
        return findUserDraftOrder(userId).orElseGet(() -> {
            OrderDto newDraft = new OrderDto();
            newDraft.setUserId(userId);
            newDraft.setStatus(OrderStatus.DRAFT);
            newDraft.setRequestedQuantities(new HashMap<>());
            return save(newDraft);
        });
    }


    public OrderDto addProductsToOrder(Long orderId, Map<Long, Integer> productsToAdd) {
        OrderDto draftOrderDto = findById(orderId);
        if (draftOrderDto.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot add products unless order is DRAFT.");
        }

        Map<Long, Integer> lines = draftOrderDto.getRequestedQuantities();
        if (lines == null) {
            lines = new HashMap<>();
        }

        StringBuilder clampWarnings = new StringBuilder();

        for (Map.Entry<Long, Integer> e : productsToAdd.entrySet()) {
            Long productId = e.getKey();
            int requestedQty = e.getValue();
            if (requestedQty <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be > 0 for product ID: " + productId);
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found with ID: " + productId));
            if (product.getQuantity() == 0) {
                throw new IllegalArgumentException(
                        "Product '" + product.getName() + "' is out of stock.");
            }
            int currentQtyInCart = lines.getOrDefault(productId, 0);
            int newTotal = currentQtyInCart + requestedQty;

            if (newTotal > product.getQuantity()) {
                clampWarnings.append(
                        product.getName()
                );
                newTotal = product.getQuantity();
            }

           if (newTotal > 0) {
                lines.put(productId, newTotal);
            } else {
                lines.remove(productId);
            }
        }

        draftOrderDto.setRequestedQuantities(lines);
    System.out.println("trying");
        if (!clampWarnings.isEmpty()) {
            throw new IllegalArgumentException("Some products are out of stock: " + clampWarnings + ". The Quantity will be set to the maximum we have.");
        }

        return save(draftOrderDto);
    }

    @Transactional
    public OrderDto deleteProductFromCart(Long orderId, Long productId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with ID: " + orderId));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot remove items unless DRAFT.");
        }

        Map<Long, Integer> lines = order.getRequestedQuantities();
        if (!lines.containsKey(productId)) {
            throw new IllegalArgumentException(
                    "Product ID " + productId + " not in cart.");
        }
        lines.remove(productId);
        double cost = calculateTotalCost(order);
        order.setTotalCost(cost);

        Order saved = orderRepository.save(order);
        return orderMapper.convertToDto(saved);
    }

    public Optional<OrderDto> findLastOrderByUserId(Long userId) {
        return orderRepository.findTopByUserIdOrderByIdDesc(userId)
                .map(orderMapper::convertToDto);
    }
}