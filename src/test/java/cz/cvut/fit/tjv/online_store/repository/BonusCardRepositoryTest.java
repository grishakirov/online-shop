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
class BonusCardRepositoryTest {

    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        bonusCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("hashed_password")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindBonusCard() {
        // given
        BonusCard newCard = new BonusCard(null, testUser, 150.0);

        // when
        BonusCard savedCard = bonusCardRepository.save(newCard);
        Optional<BonusCard> foundCard = bonusCardRepository.findById(savedCard.getId());

        // then
        assertTrue(foundCard.isPresent(), "Saved BonusCard should be retrievable by ID.");
        assertEquals(testUser.getId(), foundCard.get().getUser().getId(),
                "The associated user should match the testUser's ID.");
        assertEquals(150.0, foundCard.get().getBalance(), "The balance should match the saved value.");
    }

    @Test
    void testCannotAddMultipleBonusCardsForSingleUser() {
        // given
        BonusCard firstCard = new BonusCard(null, testUser, 100.0);
        bonusCardRepository.save(firstCard);

        BonusCard secondCard = new BonusCard(null, testUser, 200.0);

        // when
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> bonusCardRepository.save(secondCard));

        // then
        String expectedMessagePart = "could not execute statement";
        // or "unique constraint" depending on your DB dialect
        assertTrue(
                exception.getMessage().contains(expectedMessagePart),
                "Exception message should mention unique constraint violation"
        );
    }

    @Test
    void testFindByUserId() {
        // given
        BonusCard newCard = new BonusCard(null, testUser, 150.0);
        bonusCardRepository.save(newCard);

        // when
        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(testUser.getId());

        // then
        assertTrue(foundCard.isPresent(), "BonusCard should be found by user ID");
        assertEquals(150.0, foundCard.get().getBalance(), "The balance should match the saved value");
    }

    @Test
    void testFindByUserId_WhenNotFound() {
        // given (testUser has no BonusCard saved)

        // when
        Optional<BonusCard> foundCard = bonusCardRepository.findByUserId(testUser.getId());

        // then
        assertFalse(foundCard.isPresent(), "Should return empty for a user without a bonus card");
    }

    @Test
    void testDeleteBonusCard() {
        // given
        BonusCard newCard = new BonusCard(null, testUser, 150.0);
        BonusCard savedCard = bonusCardRepository.save(newCard);

        // when
        bonusCardRepository.delete(savedCard);
        Optional<BonusCard> foundCard = bonusCardRepository.findById(savedCard.getId());

        // then
        assertFalse(foundCard.isPresent(), "Deleted BonusCard should no longer be present in the repository.");
    }
}