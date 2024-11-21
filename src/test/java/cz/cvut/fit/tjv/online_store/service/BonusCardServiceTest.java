package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddBalance() {
        BonusCard bonusCard = new BonusCard(1L, null, "CARD123", 100.0);
        BonusCardDto bonusCardDto = new BonusCardDto(1L, null, "CARD123", 150.0);

        when(bonusCardRepository.findById(1L)).thenReturn(Optional.of(bonusCard));
        when(bonusCardRepository.save(bonusCard)).thenReturn(bonusCard);
        when(bonusCardMapper.convertToDto(bonusCard)).thenReturn(bonusCardDto);

        BonusCardDto result = bonusCardService.addBalance(1L, 50.0);

        assertNotNull(result);
        assertEquals(150.0, result.getBalance());
        verify(bonusCardRepository, times(1)).save(bonusCard);
    }

    @Test
    void testDeductBalance() {
        BonusCard bonusCard = new BonusCard(1L, null, "CARD123", 100.0);
        BonusCardDto bonusCardDto = new BonusCardDto(1L, null, "CARD123", 50.0);

        when(bonusCardRepository.findById(1L)).thenReturn(Optional.of(bonusCard));
        when(bonusCardRepository.save(bonusCard)).thenReturn(bonusCard);
        when(bonusCardMapper.convertToDto(bonusCard)).thenReturn(bonusCardDto);

        BonusCardDto result = bonusCardService.deductBalance(1L, 50.0);

        assertNotNull(result);
        assertEquals(50.0, result.getBalance());
        verify(bonusCardRepository, times(1)).save(bonusCard);
    }
}