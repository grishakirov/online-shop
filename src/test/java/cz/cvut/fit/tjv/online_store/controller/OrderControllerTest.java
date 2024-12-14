package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
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
import java.util.Arrays;
import java.util.List;

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
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testGetAllOrders() throws Exception {
        OrderDto order1 = new OrderDto(1L, 1L, Arrays.asList(1L, 2L), LocalDate.now(), 200.0, OrderStatus.PROCESSING);
        OrderDto order2 = new OrderDto(2L, 2L, List.of(3L), LocalDate.now(), 100.0, OrderStatus.DELIVERED);

        when(orderService.findAll()).thenReturn(Arrays.asList(order1, order2));

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
        OrderDto order = new OrderDto(1L, 1L, Arrays.asList(1L, 2L), LocalDate.now(), 200.0, OrderStatus.PROCESSING);

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
        OrderDto savedOrder = new OrderDto(1L, 1L, Arrays.asList(1L, 2L), LocalDate.now(), 200.0, OrderStatus.PROCESSING);

        when(orderService.save(any())).thenReturn(savedOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"productIds\":[1,2],\"dateOfCreation\":\"2024-12-15\",\"totalCost\":200.0,\"status\":\"PROCESSING\"}"))
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
}