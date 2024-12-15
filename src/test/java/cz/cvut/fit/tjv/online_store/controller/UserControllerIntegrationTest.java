package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.domain.*;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"}) // Use roles
    @Test
    void shouldCreateUser() throws Exception {
        String userJson = """
        {
            "name": "Jane",
            "surname": "Smith",
            "email": "jane.smith@example.com",
            "password": "password123",
            "role": "CUSTOMER"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"}) // Use roles
    @Test
    void shouldGetUserById() throws Exception {
        User user = new User(null, "Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);
        user = userRepository.save(user);

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldDeleteUserWithoutActiveOrders() throws Exception {
        // Create and save user
        User user = new User(null, "Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);
        user = userRepository.save(user);

        // Perform delete request
        mockMvc.perform(delete("/users/{id}?with-check=true", user.getId()))
                .andExpect(status().isNoContent()); // Ensure deletion is successful

        // Verify user is deleted
        assertFalse(userRepository.findById(user.getId()).isPresent());
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldNotDeleteUserWithActiveOrders() throws Exception {
        User user = new User(null, "Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);
        user = userRepository.save(user);

        Product product = new Product(null, "Product1", 100.0, 10, false, null);
        product = productRepository.save(product);

        Order order = new Order(null, user, List.of(product), LocalDate.now(), 100.0, OrderStatus.PROCESSING);
        orderRepository.save(order);

        mockMvc.perform(delete("/users/{id}?with-check=true", user.getId()))
                .andExpect(status().isConflict()) // Expect 409 Conflict
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User cannot be deleted because they have active orders."));
    }
}