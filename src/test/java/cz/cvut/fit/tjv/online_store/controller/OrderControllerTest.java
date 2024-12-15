package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllOrders() throws Exception {
        OrderDto order1 = new OrderDto(1L, 1L, Map.of(1L, 2, 2L, 1), LocalDate.now(), 200.0, OrderStatus.PROCESSING, List.of(1L, 2L));

        OrderDto order2 = new OrderDto(2L, 2L, Map.of(3L, 1), LocalDate.now(), 100.0, OrderStatus.DELIVERED, List.of(3L));

        when(orderService.findAll()).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].totalCost").value(200.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].totalCost").value(100.0));

        verify(orderService, times(1)).findAll();
    }

    @Test
    void testGetOrderById() throws Exception {
        OrderDto order = new OrderDto(1L, 1L, Map.of(1L, 2, 2L, 1), LocalDate.now(), 200.0, OrderStatus.PROCESSING, List.of(1L, 2L));

        when(orderService.findById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalCost").value(200.0))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testCreateOrder() throws Exception {
        OrderDto savedOrder = new OrderDto(1L, 1L,
                Map.of(1L, 2, 2L, 1), LocalDate.now(), 200.0, OrderStatus.PROCESSING, List.of(1L, 2L));

        when(orderService.save(any())).thenReturn(savedOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId":1,
                                    "requestedQuantities":{"1":2,"2":1},
                                    "dateOfCreation":"2024-12-15",
                                    "totalCost":200.0,
                                    "status":"PROCESSING",
                                    "productIds":[1,2]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalCost").value(200.0));

        verify(orderService, times(1)).save(any());
    }

    @Test
    void testDeleteOrder() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).delete(1L);
    }

    @Test
    void testCreateOrder_InvalidData() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "userId":1,
                            "requestedQuantities":{"1":-2},
                            "dateOfCreation":"2024-12-15",
                            "totalCost":-100.0,
                            "status":"INVALID_STATUS",
                            "productIds":[]
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUpdateOrderStatus_NullBody() throws Exception {
        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("The 'status' field is required."));
    }

    @Test
    void testUpdateOrderStatus_Conflict() throws Exception {
        when(orderService.updateStatus(1L, OrderStatus.SHIPPED))
                .thenThrow(new ConflictException("Order status update conflict"));

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "status": "SHIPPED"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Order status update conflict"));
    }

    @Test
    void testUpdateOrderStatus_InvalidStatus() throws Exception {
        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "status": "INVALID_STATUS"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid status value: INVALID_STATUS"));
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        OrderDto updatedOrder = new OrderDto(1L, 1L, Map.of(), LocalDate.now(), 200.0, OrderStatus.SHIPPED, List.of());

        when(orderService.updateStatus(1L, OrderStatus.SHIPPED)).thenReturn(updatedOrder);

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "status": "SHIPPED"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(orderService, times(1)).updateStatus(1L, OrderStatus.SHIPPED);
    }
    @Test
    void testGetAllOrders_EmptyList() throws Exception {
        when(orderService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService, times(1)).findAll();
    }
}