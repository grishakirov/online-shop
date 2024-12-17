package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BonusCardRepository bonusCardRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        bonusCardRepository.deleteAll();
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldCreateOrderWithoutBonusesIfNoBonusCardExists() throws Exception {
        User user = userRepository.save(new User(null, "John", "Doe", "john.doe@example.com", "password", LocalDate.of(2000, 1, 1), Role.CUSTOMER));
        Product product = productRepository.save(new Product(null, "Product1", 100.0, 10, false, null));

        String orderJson = String.format("""
            {
                "userId": %d,
                "requestedQuantities": {"%d": 2},
                "totalCost": 200.0,
                "status": "PROCESSING"
            }
            """, user.getId(), product.getId());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCost").value(200.0)) // No deduction
                .andExpect(jsonPath("$.id").exists());
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldUpdateOrderStatus() throws Exception {
        User user = userRepository.save(new User(null, "John", "Doe", "john.doe@example.com", "password", Role.CUSTOMER));

        Map<Long, Integer> requestedQuantities = new HashMap<>();
        Order order = new Order(null, user, requestedQuantities, LocalDate.now(), 100.0, OrderStatus.PROCESSING);
        order = orderRepository.save(order);

        mockMvc.perform(patch("/orders/{id}/status", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "status": "SHIPPED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldDeleteOrder() throws Exception {
        User user = userRepository.save(new User(null, "John", "Doe", "john.doe@example.com", "password", Role.CUSTOMER));

        Map<Long, Integer> requestedQuantities = new HashMap<>();
        Order order = new Order(null, user, requestedQuantities, LocalDate.now(), 100.0, OrderStatus.PROCESSING);
        order = orderRepository.save(order);

        mockMvc.perform(delete("/orders/{id}", order.getId()))
                .andExpect(status().isNoContent());

        assertFalse(orderRepository.findById(order.getId()).isPresent());
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldApplyBonusCardAndAddCashback() throws Exception {
        User user = userRepository.save(new User(null, "John", "Doe", "john.doe@example.com", "password", LocalDate.of(2000, 1, 1), Role.CUSTOMER));
        Product product = productRepository.save(new Product(null, "Product1", 100.0, 10, false, null));

        BonusCard bonusCard = new BonusCard();
        bonusCard.setUser(user);
        bonusCard.setBalance(50.0);
        bonusCard.setCardNumber("1234567890");
        bonusCardRepository.save(bonusCard);

        String orderJson = String.format("""
            {
                "userId": %d,
                "requestedQuantities": {"%d": 2},
                "totalCost": 200.0,
                "status": "PROCESSING"
            }
            """, user.getId(), product.getId());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCost").value(150.0))
                .andExpect(jsonPath("$.id").exists());

        BonusCard updatedBonusCard = bonusCardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Bonus card not found"));
        assertEquals(7.5, updatedBonusCard.getBalance());
    }
}