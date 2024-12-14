package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BonusCardControllerTest {

    @Mock
    private BonusCardService bonusCardService;

    @InjectMocks
    private BonusCardController bonusCardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bonusCardController).build();
    }

    @Test
    void testGetAllBonusCards() throws Exception {
        BonusCardDto card1 = new BonusCardDto(1L, 1L, "CARD123", 100.0);
        BonusCardDto card2 = new BonusCardDto(2L, 2L, "CARD456", 200.0);

        when(bonusCardService.findAll()).thenReturn(Arrays.asList(card1, card2));

        mockMvc.perform(get("/bonus-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cardNumber").value("CARD123"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].cardNumber").value("CARD456"));

        verify(bonusCardService, times(1)).findAll();
    }

    @Test
    void testGetBonusCardById() throws Exception {
        BonusCardDto card = new BonusCardDto(1L, 1L, "CARD123", 100.0);

        when(bonusCardService.findById(1L)).thenReturn(card);

        mockMvc.perform(get("/bonus-cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("CARD123"));

        verify(bonusCardService, times(1)).findById(1L);
    }

    @Test
    void testCreateBonusCard() throws Exception {
        BonusCardDto savedCard = new BonusCardDto(1L, 1L, "CARD123", 100.0);

        when(bonusCardService.save(any())).thenReturn(savedCard);

        mockMvc.perform(post("/bonus-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"cardNumber\":\"CARD123\",\"balance\":100.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("CARD123"));

        verify(bonusCardService, times(1)).save(any());
    }

    @Test
    void testDeleteBonusCard() throws Exception {
        doNothing().when(bonusCardService).delete(1L);

        mockMvc.perform(delete("/bonus-cards/1"))
                .andExpect(status().isNoContent());

        verify(bonusCardService, times(1)).delete(1L);
    }

    @Test
    void testFindBonusCardByCardNumber() throws Exception {
        BonusCardDto card = new BonusCardDto(1L, 1L, "CARD123", 100.0);

        when(bonusCardService.findByCardNumber("CARD123")).thenReturn(card);

        mockMvc.perform(get("/bonus-cards/find-by-card-number/CARD123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("CARD123"));

        verify(bonusCardService, times(1)).findByCardNumber("CARD123");
    }

    @Test
    void testAddBalance() throws Exception {
        BonusCardDto updatedCard = new BonusCardDto(1L, 1L, "CARD123", 150.0);

        when(bonusCardService.addBalance(1L, 50.0)).thenReturn(updatedCard);

        mockMvc.perform(post("/bonus-cards/1/add-balance")
                        .param("amount", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));

        verify(bonusCardService, times(1)).addBalance(1L, 50.0);
    }

    @Test
    void testDeductBalance() throws Exception {
        BonusCardDto updatedCard = new BonusCardDto(1L, 1L, "CARD123", 50.0);

        when(bonusCardService.deductBalance(1L, 50.0)).thenReturn(updatedCard);

        mockMvc.perform(post("/bonus-cards/1/deduct-balance")
                        .param("amount", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.0));

        verify(bonusCardService, times(1)).deductBalance(1L, 50.0);
    }
}