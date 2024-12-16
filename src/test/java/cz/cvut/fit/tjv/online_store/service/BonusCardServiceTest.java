package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BonusCardServiceTest {

    @Mock
    private BonusCardRepository bonusCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BonusCardMapper bonusCardMapper;

    @InjectMocks
    private BonusCardService bonusCardService;

    private User testUser;
    private BonusCard testBonusCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        testBonusCard = new BonusCard(1L, testUser, "CARD123", 100.0);
    }

    @Test
    void testSave_Success() {
        BonusCardDto bonusCardDto = new BonusCardDto(null, testUser.getId(), "CARD123", 100.0);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(bonusCardMapper.convertToEntity(bonusCardDto)).thenReturn(testBonusCard);
        when(bonusCardRepository.save(testBonusCard)).thenReturn(testBonusCard);
        when(bonusCardMapper.convertToDto(testBonusCard)).thenReturn(bonusCardDto);

        BonusCardDto result = bonusCardService.save(bonusCardDto);

        assertNotNull(result);
        assertEquals("CARD123", result.getCardNumber());
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(bonusCardRepository, times(1)).save(testBonusCard);
    }

    @Test
    void testSave_UserNotFound() {
        BonusCardDto bonusCardDto = new BonusCardDto(null, 2L, "CARD123", 100.0);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.save(bonusCardDto));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(2L);
        verifyNoInteractions(bonusCardMapper);
        verifyNoInteractions(bonusCardRepository);
    }

    @Test
    void testAddBalance_Success() {
        BonusCardDto expectedDto = new BonusCardDto(1L, 1L, "CARD123", 150.0);
        when(bonusCardRepository.findById(testBonusCard.getId())).thenReturn(Optional.of(testBonusCard));
        when(bonusCardRepository.save(testBonusCard)).thenReturn(testBonusCard);
        when(bonusCardMapper.convertToDto(testBonusCard)).thenReturn(expectedDto);

        BonusCardDto updatedCard = bonusCardService.addBalance(testBonusCard.getId(), 50.0);

        assertNotNull(updatedCard, "Updated BonusCardDto should not be null");
        assertEquals(150.0, updatedCard.getBalance(), "Balance should be updated to 150.0");
        verify(bonusCardRepository, times(1)).findById(testBonusCard.getId());
        verify(bonusCardRepository, times(1)).save(testBonusCard);
        verify(bonusCardMapper, times(1)).convertToDto(testBonusCard);
    }

    @Test
    void testAddBalance_NegativeAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.addBalance(1L, -10.0));
        assertEquals("Amount must be greater than 0", exception.getMessage());

        verifyNoInteractions(bonusCardRepository);
    }

    @Test
    void testAddBalance_CardNotFound() {
        when(bonusCardRepository.findById(testBonusCard.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.addBalance(testBonusCard.getId(), 50.0));
        assertEquals("Bonus card not found", exception.getMessage());

        verify(bonusCardRepository, times(1)).findById(testBonusCard.getId());
        verifyNoMoreInteractions(bonusCardRepository);
    }

    @Test
    void testDeductBalance_Success() {
        BonusCardDto expectedDto = new BonusCardDto(1L, 1L, "CARD123", 50.0);
        when(bonusCardRepository.findById(testBonusCard.getId())).thenReturn(Optional.of(testBonusCard));
        when(bonusCardRepository.save(testBonusCard)).thenReturn(testBonusCard);
        when(bonusCardMapper.convertToDto(testBonusCard)).thenReturn(expectedDto);

        BonusCardDto updatedCard = bonusCardService.deductBalance(testBonusCard.getId(), 50.0);

        assertNotNull(updatedCard, "Updated BonusCardDto should not be null");
        assertEquals(50.0, updatedCard.getBalance(), "Balance should be reduced to 50.0");
        verify(bonusCardRepository, times(1)).findById(testBonusCard.getId());
        verify(bonusCardRepository, times(1)).save(testBonusCard);
        verify(bonusCardMapper, times(1)).convertToDto(testBonusCard);
    }

    @Test
    void testDeductBalance_InsufficientFunds() {
        when(bonusCardRepository.findById(testBonusCard.getId())).thenReturn(Optional.of(testBonusCard));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.deductBalance(testBonusCard.getId(), 150.0));
        assertEquals("Insufficient balance on the bonus card", exception.getMessage());

        verify(bonusCardRepository, times(1)).findById(testBonusCard.getId());
        verifyNoMoreInteractions(bonusCardRepository);
    }

    @Test
    void testGetBalanceByUserId_Success() {
        when(bonusCardRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBonusCard));

        Double balance = bonusCardService.getBalanceByUserId(testUser.getId());

        assertEquals(100.0, balance);
        verify(bonusCardRepository, times(1)).findByUserId(testUser.getId());
    }

    @Test
    void testGetBalanceByUserId_CardNotFound() {
        when(bonusCardRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.getBalanceByUserId(testUser.getId()));
        assertEquals("Bonus card not found for user", exception.getMessage());

        verify(bonusCardRepository, times(1)).findByUserId(testUser.getId());
    }

    @Test
    void testAddCashback_Success() {
        BonusCardDto expectedDto = new BonusCardDto(1L, 1L, "CARD123", 110.0);
        when(bonusCardRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBonusCard));
        when(bonusCardRepository.save(testBonusCard)).thenReturn(testBonusCard);

        when(bonusCardMapper.convertToDto(testBonusCard)).thenReturn(expectedDto);

        BonusCardDto updatedCard = bonusCardService.addCashback(testUser.getId(), 10.0);

        assertNotNull(updatedCard, "Updated BonusCardDto should not be null");
        assertEquals(110.0, updatedCard.getBalance(), "Balance should be updated to 110.0");
        verify(bonusCardRepository, times(1)).findByUserId(testUser.getId());
        verify(bonusCardRepository, times(1)).save(testBonusCard);
        verify(bonusCardMapper, times(1)).convertToDto(testBonusCard);
    }

    @Test
    void testAddCashback_CardNotFound() {
        when(bonusCardRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bonusCardService.addCashback(testUser.getId(), 10.0));
        assertEquals("Bonus card not found for user", exception.getMessage());

        verify(bonusCardRepository, times(1)).findByUserId(testUser.getId());
    }
}