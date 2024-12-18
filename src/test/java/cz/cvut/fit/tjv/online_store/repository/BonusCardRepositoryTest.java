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

    @BeforeEach
    void setUp() {
        bonusCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(null, "John", "Doe", "john.doe@example.com", "hashed_password", LocalDate.of(1990, 1, 1));
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindBonusCard() {
        BonusCard newCard = new BonusCard(null, testUser, 150.0);
        BonusCard savedCard = bonusCardRepository.save(newCard);

        Optional<BonusCard> foundCard = bonusCardRepository.findById(savedCard.getId());
        assertTrue(foundCard.isPresent(), "Saved BonusCard should be retrievable by ID");
        assertEquals(testUser.getId(), foundCard.get().getUser().getId(), "The associated user should match");
        assertEquals(150.0, foundCard.get().getBalance(), "The balance should match");
    }

    @Test
    void testCannotAddMultipleBonusCardsForUser() {
        BonusCard firstCard = new BonusCard(null, testUser, 100.0);
        bonusCardRepository.save(firstCard);

        BonusCard secondCard = new BonusCard(null, testUser, 200.0);
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            bonusCardRepository.save(secondCard);
        });

        String expectedMessage = "Unique index or primary key violation";
        assertTrue(exception.getMessage().contains(expectedMessage),
                "Exception message should indicate a unique constraint violation");
    }

    @Test
    void testFindByUserId() {
        BonusCard newCard = new BonusCard(null, testUser, 150.0);
        bonusCardRepository.save(newCard);

        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(testUser.getId());
        assertTrue(foundCard.isPresent(), "BonusCard should be found by user ID");
        assertEquals(150.0, foundCard.get().getBalance(), "The balance should match");
    }

    @Test
    void testFindByUserId_NotFound() {
        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(999L);
        assertFalse(foundCard.isPresent(), "No BonusCard should be found for a non-existent user ID");
    }

    @Test
    void testDeleteBonusCard() {
        BonusCard newCard = new BonusCard(null, testUser, 150.0);
        BonusCard savedCard = bonusCardRepository.save(newCard);

        bonusCardRepository.delete(savedCard);

        Optional<BonusCard> foundCard = bonusCardRepository.findById(savedCard.getId());
        assertFalse(foundCard.isPresent(), "Deleted BonusCard should not be found");
    }
}