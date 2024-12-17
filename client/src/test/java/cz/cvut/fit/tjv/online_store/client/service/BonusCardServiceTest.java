package cz.cvut.fit.tjv.online_store.client.service;

import cz.cvut.fit.tjv.online_store.client.controller.dto.BonusCardDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class BonusCardServiceTest {

    @Autowired
    private BonusCardService bonusCardService;

    @Test
    void testGetAllBonusCards() {
        List<BonusCardDto> bonusCards = (List<BonusCardDto>) bonusCardService.getAll();
        System.out.println("Fetched bonus cards: " + bonusCards);
        System.out.println(bonusCards.getFirst().getUserId());
        assertFalse(bonusCards.isEmpty(), "Bonus cards should not be empty");
        assertEquals(1, bonusCards.size());
        assertEquals("CARD123", bonusCards.get(0).getCardNumber());
    }
}
