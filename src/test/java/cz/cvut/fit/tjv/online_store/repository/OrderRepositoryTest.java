package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.Order;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;


    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User(null, "John", "Doe", "john.doe@example.com", "hashed_password", LocalDate.of(1990, 1, 1)));

        orderRepository.save(new Order(null, testUser, List.of(), LocalDate.now(), 100.0, OrderStatus.PROCESSING));
        orderRepository.save(new Order(null, testUser, List.of(), LocalDate.now(), 200.0, OrderStatus.SHIPPED));
    }

    @Test
    void shouldReturnTrueIfUserHasOrdersWithMatchingStatuses() {
        boolean result = orderRepository.existsByUserIdAndStatusIn(testUser.getId(), List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
        assertTrue(result, "User should have orders with matching statuses");
    }

    @Test
    void shouldReturnFalseIfUserHasNoOrdersWithMatchingStatuses() {
        boolean result = orderRepository.existsByUserIdAndStatusIn(testUser.getId(), List.of(OrderStatus.CANCELED));
        assertFalse(result, "User should not have orders with non-matching statuses");
    }

    @Test
    void shouldReturnFalseIfUserHasNoOrders() {
        User newUser = userRepository.save(new User(null, "Jane", "Smith", "jane.smith@example.com", "hashed_password", LocalDate.of(1992, 3, 15)));
        boolean result = orderRepository.existsByUserIdAndStatusIn(newUser.getId(), List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
        assertFalse(result, "User with no orders should return false");
    }
}