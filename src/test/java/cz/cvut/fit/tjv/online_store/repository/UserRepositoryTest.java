package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail_UserExists() {
        userRepository.save(User.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("hashed_password")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build());

        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals("John", foundUser.get().getName());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail_UserDoesNotExist() {
        Optional<User> foundUser = userRepository.findByEmail("non.existent@example.com");

        assertFalse(foundUser.isPresent(), "No user should be found");
    }
}