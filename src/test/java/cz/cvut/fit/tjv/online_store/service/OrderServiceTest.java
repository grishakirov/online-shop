package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.Order;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.domain.Product;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        Order order = new Order(1L, new User(), List.of(), LocalDate.now(), 100.0, null);
        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(), LocalDate.now(), 100.0, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.convertToDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_OrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.findById(1L));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void testSaveOrder_Success() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        Product product = new Product(1L, "Product1", 50.0, 10, false, null);
        Order order = new Order(1L, user, List.of(product), LocalDate.now(), 100.0, null);
        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(1L, 2), LocalDate.now(), 100.0, OrderStatus.PROCESSING, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.convertToEntity(orderDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.convertToDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.save(orderDto);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findAllById(List.of(1L));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testSaveOrder_UserNotFound() {
        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(1L, 2), LocalDate.now(), 100.0, null, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.save(orderDto));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void testSaveOrder_ProductOutOfStock() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        Product product = new Product(1L, "Product1", 50.0, 0, false, null);

        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(1L, 2), LocalDate.now(), 100.0, OrderStatus.PROCESSING, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.save(orderDto));
        assertEquals("Product Product1 is out of stock.", exception.getMessage()); // Updated message

        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findAllById(List.of(1L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void testSaveOrder_AgeRestrictedProduct() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(2010, 11, 11));
        Product product = new Product(1L, "RestrictedProduct", 50.0, 10, true, 18);

        OrderDto orderDto = new OrderDto(1L, 1L, Map.of(1L, 2), LocalDate.now(), 100.0, OrderStatus.PROCESSING, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderService.save(orderDto));
        assertEquals("User is too young to purchase product: RestrictedProduct", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findAllById(List.of(1L));
        verifyNoInteractions(orderMapper);
    }
}