package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.configuration.SecurityConfiguration;
import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import cz.cvut.fit.tjv.online_store.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BonusCardController.class)
@Import(SecurityConfiguration.class)
class BonusCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private BonusCardService bonusCardService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testGetAllBonusCards() throws Exception {
        BonusCardDto card1 = new BonusCardDto(1L, 1L, 100.0);
        BonusCardDto card2 = new BonusCardDto(2L, 2L, 200.0);

        when(bonusCardService.findAll()).thenReturn(Arrays.asList(card1, card2));

        mockMvc.perform(get("/api/admin/bonus-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].balance").value(100.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].balance").value(200.0));

        verify(bonusCardService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testGetBonusCardById() throws Exception {
        BonusCardDto card = new BonusCardDto(1L, 1L, 100.0);

        when(bonusCardService.findById(1L)).thenReturn(card);

        mockMvc.perform(get("/api/admin/bonus-cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(100.0));

        verify(bonusCardService, times(1)).findById(1L);
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBonusCard_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/bonus-cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"balance\":100.0}"))
                .andExpect(status().isForbidden());

        verify(bonusCardService, times(0)).save(any(BonusCardDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testDeleteBonusCard() throws Exception {
        doNothing().when(bonusCardService).delete(1L);

        mockMvc.perform(delete("/api/admin/bonus-cards/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bonusCardService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testDeleteBonusCard_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Bonus card not found.")).when(bonusCardService).delete(99L);

        mockMvc.perform(delete("/api/admin/bonus-cards/99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Bonus card not found."));

        verify(bonusCardService, times(1)).delete(99L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testAddBalance() throws Exception {
        BonusCardDto updatedCard = new BonusCardDto(1L, 1L, 150.0);

        when(bonusCardService.addBalance(1L, 50.0)).thenReturn(updatedCard);

        mockMvc.perform(post("/api/admin/bonus-cards/1/add-balance")
                        .with(csrf())
                        .param("amount", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));

        verify(bonusCardService, times(1)).addBalance(1L, 50.0);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testAddBalance_InvalidAmount() throws Exception {
        when(bonusCardService.addBalance(1L, -50.0))
                .thenThrow(new IllegalArgumentException("Invalid amount"));

        mockMvc.perform(post("/api/admin/bonus-cards/1/add-balance")
                        .with(csrf())
                        .param("amount", "-50.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid amount"));

        verify(bonusCardService, times(1)).addBalance(1L, -50.0);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testDeductBalance() throws Exception {
        BonusCardDto updatedCard = new BonusCardDto(1L, 1L, 50.0);

        when(bonusCardService.deductBalance(1L, 50.0)).thenReturn(updatedCard);

        mockMvc.perform(post("/api/admin/bonus-cards/1/deduct-balance")
                        .with(csrf())
                        .param("amount", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.0));

        verify(bonusCardService, times(1)).deductBalance(1L, 50.0);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void testDeductBalance_InsufficientFunds() throws Exception {
        when(bonusCardService.deductBalance(1L, 150.0))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        mockMvc.perform(post("/api/admin/bonus-cards/1/deduct-balance")
                        .with(csrf())
                        .param("amount", "150.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Insufficient balance"));

        verify(bonusCardService, times(1)).deductBalance(1L, 150.0);
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAccessBonusCards_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/bonus-cards"))
                .andExpect(status().isForbidden());
    }

}