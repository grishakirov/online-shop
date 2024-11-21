package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.Order;
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
import java.util.Collections;
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
    void testFindById() {
        Order order = new Order(1L, new User(), Collections.emptyList(), null, 100.0, null);
        OrderDto orderDto = new OrderDto(1L, 1L, Collections.emptyList(), null, 100.0, null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.convertToDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveOrder() {

        User user = new User(1L, "John Doe", "johnd", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        Order order = new Order(1L, user, Collections.emptyList(), null, 100.0, null);
        OrderDto orderDto = new OrderDto(1L, 1L, Collections.emptyList(), null, 100.0, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(orderMapper.convertToEntity(orderDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.convertToDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.save(orderDto);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(userRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }
}