package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.repository.*;
import cz.cvut.fit.tjv.online_store.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BonusCardRepository bonusCardRepository;

    @Mock
    private BonusCardService bonusCardService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveOrder_WithBonusCardDeduction() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        Product product = new Product(1L, "Product1", 50.0, 10, false, null);
        BonusCard bonusCard = new BonusCard(1L, user, "CARD123", 30.0);

        Map<Long, Integer> requestedQuantities = Map.of(1L, 2);
        OrderDto orderDto = new OrderDto(1L, 1L, requestedQuantities, LocalDate.now(), 70.0, OrderStatus.PROCESSING, List.of(1L));
        Order savedOrder = new Order(1L, user, requestedQuantities, LocalDate.now(), 70.0, OrderStatus.PROCESSING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bonusCardRepository.findByUserId(1L)).thenReturn(Optional.of(bonusCard));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderMapper.convertToEntity(any(OrderDto.class))).thenReturn(savedOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.convertToDto(any(Order.class))).thenReturn(orderDto);

        OrderDto result = orderService.save(orderDto);

        assertNotNull(result);
        assertEquals(70.0, result.getTotalCost(), "Bonus card deduction failed");
        verify(bonusCardService).deductBalance(1L, 30.0);
        verify(bonusCardService).addBalance(1L, 3.5);
        verify(productRepository, times(2)).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testSaveOrder_ExistingDraftOrder() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        Product product = new Product(1L, "Product1", 50.0, 10, false, null);

        Map<Long, Integer> existingQuantities = new HashMap<>(Map.of(1L, 2));
        Map<Long, Integer> newQuantities = Map.of(1L, 3);

        Order existingOrder = new Order(1L, user, existingQuantities, LocalDate.now(), 100.0, OrderStatus.DRAFT);
        OrderDto updatedOrderDto = new OrderDto(1L, 1L, newQuantities, LocalDate.now(), 150.0, OrderStatus.DRAFT, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        when(orderMapper.convertToDto(existingOrder)).thenReturn(updatedOrderDto);

        OrderDto result = orderService.save(updatedOrderDto);

        assertNotNull(result);
        assertEquals(150.0, result.getTotalCost());
        assertEquals(3, existingOrder.getRequestedQuantities().get(1L));
        verify(productRepository, times(1)).findAllById(any());
        verify(productRepository, times(2)).findById(1L);
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    void testSaveOrder_InvalidQuantity() {
        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(1L, 0), LocalDate.now(), 100.0, OrderStatus.DRAFT, List.of(1L));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.save(orderDto));
        assertEquals("Requested quantities cannot be null, empty, or contain non-positive values.", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void testFindById_Success() {
        Order order = new Order(1L, new User(), Map.of(), LocalDate.now(), 100.0, OrderStatus.PROCESSING);
        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(), LocalDate.now(), 100.0, OrderStatus.PROCESSING, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.convertToDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.findById(1L));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        assertDoesNotThrow(() -> orderService.delete(1L));

        verify(orderRepository, times(1)).existsById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.delete(1L));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).existsById(1L);
        verify(orderRepository, never()).deleteById(1L);
    }
}