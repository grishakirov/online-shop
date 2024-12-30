package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.domain.Role;
import cz.cvut.fit.tjv.online_store.domain.Order;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.domain.Product;
import cz.cvut.fit.tjv.online_store.domain.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    private User createUser(String name, String surname, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Product createProduct(String name, double price, int quantity, boolean isRestricted, Integer allowedAge) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setIsRestricted(isRestricted);
        product.setAllowedAge(allowedAge);
        return productRepository.save(product);
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
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

        mockMvc.perform(post("/users/registr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldGetUserById() throws Exception {
        User user = createUser("Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldDeleteUserWithoutActiveOrders() throws Exception {
        User user = createUser("Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);

        mockMvc.perform(delete("/users/{id}?with-check=true", user.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(user.getId()).isPresent());
    }

    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    @Test
    void shouldNotDeleteUserWithActiveOrders() throws Exception {
        User user = createUser("Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);
        Product product = createProduct("Product1", 100.0, 10, false, null);

        Map<Long, Integer> requestedQuantities = new HashMap<>();
        requestedQuantities.put(product.getId(), 2);

        Order order = new Order();
        order.setUser(user);
        order.setRequestedQuantities(requestedQuantities);
        order.setDateOfCreation(LocalDate.now());
        order.setTotalCost(200.0);
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        mockMvc.perform(delete("/users/{id}?with-check=true", user.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("User cannot be deleted because they have active orders.")));
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void shouldNotAllowDeletionWithoutAdminRights() throws Exception {
        User user = createUser("Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);

        mockMvc.perform(delete("/users/{id}?with-check=true", user.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"CUSTOMER"})
    void shouldReturnAuthenticatedUser() throws Exception {
        createUser("Test", "User", "testuser@example.com", "password123", Role.CUSTOMER);

        mockMvc.perform(get("/users/authenticated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    void shouldNotCreateUserWithExistingEmail() throws Exception {
        createUser("Jane", "Smith", "jane.smith@example.com", "password123", Role.CUSTOMER);

        String userJson = """
    {
        "name": "Jane",
        "surname": "Doe",
        "email": "jane.smith@example.com",
        "password": "newpassword123",
        "role": "CUSTOMER"
    }
    """;

        mockMvc.perform(post("/users/registr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Email already exists")));
    }
}