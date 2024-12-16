package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BonusCardRepositoryTest {

    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private BonusCard testBonusCard;

    @BeforeEach
    void setUp() {
        bonusCardRepository.deleteAll();
        userRepository.deleteAll();
        testUser = new User(null, "John", "Doe", "john.doe@example.com", "hashed_password", LocalDate.of(1990, 1, 1));
        testUser = userRepository.save(testUser);

        testBonusCard = new BonusCard(null, testUser, "CARD123", 100.0);
        testBonusCard = bonusCardRepository.save(testBonusCard);
    }

    @Test
    void testFindByCardNumber() {
        Optional<BonusCard> foundCard = bonusCardRepository.findByCardNumber("CARD123");

        assertTrue(foundCard.isPresent(), "BonusCard should be found by card number");
        assertEquals("CARD123", foundCard.get().getCardNumber(), "Card number should match");
        assertEquals(testUser.getId(), foundCard.get().getUser().getId(), "The associated user should match");
        assertEquals(100.0, foundCard.get().getBalance(), "The balance should match");
    }

    @Test
    void testFindByCardNumber_NotFound() {
        Optional<BonusCard> foundCard = bonusCardRepository.findByCardNumber("INVALID_CARD");
        assertFalse(foundCard.isPresent(), "No BonusCard should be found for an invalid card number");
    }

    @Test
    void testFindByUserId() {
        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(testUser.getId());

        assertTrue(foundCard.isPresent(), "BonusCard should be found by user ID");
        assertEquals("CARD123", foundCard.get().getCardNumber(), "Card number should match");
        assertEquals(testUser.getId(), foundCard.get().getUser().getId(), "The associated user should match");
        assertEquals(100.0, foundCard.get().getBalance(), "The balance should match");
    }

    @Test
    void testFindByUserId_NotFound() {
        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(999L);
        assertFalse(foundCard.isPresent(), "No BonusCard should be found for a non-existent user ID");
    }

    @Test
    void testSaveBonusCard() {
        BonusCard duplicateCard = new BonusCard(null, testUser, "CARD456", 200.0);

        assertThrows(DataIntegrityViolationException.class, () -> bonusCardRepository.save(duplicateCard));
    }

    @Test
    void testDeleteBonusCard() {
        bonusCardRepository.delete(testBonusCard);
        Optional<BonusCard> foundCard = bonusCardRepository.findByCardNumber("CARD123");
        assertFalse(foundCard.isPresent(), "Deleted BonusCard should not be found");
    }
}