package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BonusCardRepositoryTest {

    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByCardNumber() {
        User user = new User(null, "John", "Doe", "john.doe@example.com", "hashed_password", LocalDate.of(1990, 1, 1));
        user = userRepository.save(user);

        bonusCardRepository.save(new BonusCard(null, user, "CARD123", 100.0));

        Optional<BonusCard> foundCard = bonusCardRepository.findByCardNumber("CARD123");

        assertTrue(foundCard.isPresent(), "BonusCard should be found");
        assertEquals("CARD123", foundCard.get().getCardNumber(), "Card number should match");
        assertEquals(user.getId(), foundCard.get().getUser().getId(), "The associated user should match");
        assertEquals(user.getEmail(), foundCard.get().getUser().getEmail(), "The user's email should match");
    }
}