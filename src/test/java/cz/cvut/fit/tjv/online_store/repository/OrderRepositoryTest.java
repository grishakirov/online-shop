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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        orderRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(
                User.builder()
                        .name("John")
                        .surname("Doe")
                        .email("john.doe@example.com")
                        .password("hashed_password")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .build()
        );
        orderRepository.save(
                Order.builder()
                        .user(testUser)
                        .requestedQuantities(Map.of())
                        .dateOfCreation(LocalDate.now())
                        .totalCost(100.0)
                        .status(OrderStatus.PROCESSING)
                        .build()
        );
        orderRepository.save(
                Order.builder()
                        .user(testUser)
                        .requestedQuantities(Map.of())
                        .dateOfCreation(LocalDate.now())
                        .totalCost(200.0)
                        .status(OrderStatus.SHIPPED)
                        .build()
        );
    }

    @Test
    void testExistsByUserIdAndStatusIn_ShouldReturnTrue() {
        List<OrderStatus> statusesToCheck = List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        boolean result = orderRepository.existsByUserIdAndStatusIn(testUser.getId(), statusesToCheck);
        assertTrue(result, "User has orders with PROCESSING or SHIPPED, so result should be true.");
    }

    @Test
    void testExistsByUserIdAndStatusIn_ShouldReturnFalseForDifferentStatus() {
        List<OrderStatus> statusesToCheck = List.of(OrderStatus.CANCELED);
        boolean result = orderRepository.existsByUserIdAndStatusIn(testUser.getId(), statusesToCheck);
        assertFalse(result, "User does not have any orders with CANCELED status, so should be false.");
    }

    @Test
    void testExistsByUserIdAndStatusIn_WhenNoOrdersForUser() {
        User newUser = userRepository.save(
                User.builder()
                        .name("Jane")
                        .surname("Smith")
                        .email("jane.smith@example.com")
                        .password("hashed_password")
                        .birthDate(LocalDate.of(1992, 3, 15))
                        .build()
        );
        List<OrderStatus> statusesToCheck = List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        boolean result = orderRepository.existsByUserIdAndStatusIn(newUser.getId(), statusesToCheck);
        assertFalse(result, "New user has no orders, so should return false for any status check.");
    }
}