package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.repository.*;
import cz.cvut.fit.tjv.online_store.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
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
    private User testUser;
    private Product testProduct;
    private BonusCard testBonusCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "raw_pass",
                LocalDate.of(1999, 11, 11)
        );
        testProduct = new Product(
                1L,
                "Product1",
                50.0,
                10,
                false,
                null
        );
        testBonusCard = new BonusCard(
                1L,
                testUser,
                30.0
        );
    }

    @Test
    void testSaveOrder_ExistingDraftOrder() {
        // Domain data
        Map<Long, Integer> existingQty = new HashMap<>(Map.of(1L, 2));
        Map<Long, Integer> newQty = Map.of(1L, 3);
        Order existingDraft = new Order(
                1L,
                testUser,
                existingQty,
                LocalDate.now(),
                100.0,
                OrderStatus.DRAFT,
                0.0
        );
        OrderDto newDto = new OrderDto(
                1L,
                1L,
                newQty,
                LocalDate.now(),
                150.0,
                OrderStatus.DRAFT,
                List.of(1L)
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingDraft));
        when(orderRepository.save(any(Order.class))).thenReturn(existingDraft);
        when(orderMapper.convertToDto(existingDraft)).thenReturn(newDto);
        OrderDto result = orderService.save(newDto);
        assertNotNull(result);
        assertEquals(150.0, result.getTotalCost());
        assertEquals(Integer.valueOf(3), existingDraft.getRequestedQuantities().get(1L));
        verify(orderRepository).save(existingDraft);
    }
    @Test
    void testSaveOrder_WithBonusCardDeduction() {
        Map<Long, Integer> requestedQuantities = Map.of(1L, 2);
        OrderDto inputDto = new OrderDto(
                1L,
                1L,
                requestedQuantities,
                LocalDate.now(),
                70.0,
                OrderStatus.PROCESSING,
                List.of(1L)
        );
        Order domainOrder = new Order(
                1L,
                testUser,
                requestedQuantities,
                LocalDate.now(),
                70.0,
                OrderStatus.PROCESSING,
                0.0
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bonusCardRepository.findByUserId(1L)).thenReturn(Optional.of(testBonusCard));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderMapper.convertToEntity(any(OrderDto.class))).thenReturn(domainOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(domainOrder);
        when(orderMapper.convertToDto(domainOrder)).thenReturn(inputDto);
        OrderDto result = orderService.save(inputDto);
        assertNotNull(result);
        assertEquals(70.0, result.getTotalCost(), "Incorrect total cost returned");
        verify(productRepository, times(2)).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testFindById_Success() {
        Order order = new Order(
                1L,
                testUser,
                Map.of(),
                LocalDate.now(),
                100.0,
                OrderStatus.PROCESSING,
                0.0
        );
        OrderDto dto = new OrderDto(
                1L,
                1L,
                Map.of(),
                LocalDate.now(),
                100.0,
                OrderStatus.PROCESSING,
                List.of()
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.convertToDto(order)).thenReturn(dto);

        OrderDto result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalCost());
        verify(orderRepository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.findById(1L)
        );
        assertEquals("Order not found", ex.getMessage());
        verify(orderRepository).findById(1L);
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        assertDoesNotThrow(() -> orderService.delete(1L));
        verify(orderRepository).existsById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.delete(1L)
        );
        assertEquals("Order not found", ex.getMessage());
        verify(orderRepository).existsById(1L);
        verify(orderRepository, never()).deleteById(anyLong());
    }
}