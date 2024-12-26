package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import cz.cvut.fit.tjv.online_store.service.CustomUserDetailsService;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserBonusCardController.class)
class UserBonusCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BonusCardService bonusCardService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BonusCardRepository bonusCardRepository;

    @MockBean
    private BonusCardMapper bonusCardMapper;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetCurrentUserBonusCard_Success() throws Exception {
        String email = "user@example.com";
        Long userId = 1L;
        BonusCardDto bonusCardDto = new BonusCardDto(1L, userId, 100.0);
        var user = new cz.cvut.fit.tjv.online_store.domain.User();
        user.setId(userId);
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var bonusCard = new cz.cvut.fit.tjv.online_store.domain.BonusCard();
        bonusCard.setId(1L);
        bonusCard.setUser(user);
        bonusCard.setBalance(100.0);
        when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(bonusCard));
        when(bonusCardMapper.convertToDto(bonusCard)).thenReturn(bonusCardDto);
        mockMvc.perform(get("/api/users/my/bonus-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(100.0));

        verify(userRepository, times(1)).findByEmail(email);
        verify(bonusCardRepository, times(1)).findByUserId(userId);
        verify(bonusCardMapper, times(1)).convertToDto(bonusCard);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetCurrentUserBonusCard_BonusCardNotFound() throws Exception {
        String email = "user@example.com";
        Long userId = 1L;

        var user = new cz.cvut.fit.tjv.online_store.domain.User();
        user.setId(userId);
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/my/bonus-card"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Bonus card not found."));

        verify(userRepository, times(1)).findByEmail(email);
        verify(bonusCardRepository, times(1)).findByUserId(userId);
        verifyNoInteractions(bonusCardMapper);
    }


    @Test
    void testGetCurrentUserBonusCard_Unauthenticated() throws Exception {

        mockMvc.perform(get("/api/users/my/bonus-card"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(userRepository, bonusCardRepository, bonusCardMapper);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetCurrentUserBonusCard_UserNotFound() throws Exception {
        String email = "user@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/my/bonus-card"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found."));

        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(bonusCardRepository, bonusCardMapper);
    }
    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateForCurrentUser_Success() throws Exception {
        String email = "user@example.com";
        Long userId = 1L;
        BonusCardDto createdBonusCardDto = new BonusCardDto(1L, userId, 100.0);
        var user = new cz.cvut.fit.tjv.online_store.domain.User();
        user.setId(userId);
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bonusCardService.createForUser(email)).thenReturn(createdBonusCardDto);
        mockMvc.perform(post("/api/users/my/bonus-card")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(100.0));

        verify(bonusCardService, times(1)).createForUser(email);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateForCurrentUser_UserAlreadyHasBonusCard() throws Exception {
        String email = "user@example.com";
        var user = new cz.cvut.fit.tjv.online_store.domain.User();
        user.setId(1L);
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bonusCardService.createForUser(email))
                .thenThrow(new ConflictException("User already has a bonus card"));
        mockMvc.perform(post("/api/users/my/bonus-card")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User already has a bonus card"));

        verify(bonusCardService, times(1)).createForUser(email);
    }

    @Test
    void testCreateForCurrentUser_Unauthenticated() throws Exception {


        mockMvc.perform(post("/api/users/my/bonus-card")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"balance\":100.0}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bonusCardService, userRepository, bonusCardRepository, bonusCardMapper);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateForCurrentUser_UserNotFound() throws Exception {
        String email = "user@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(bonusCardService.createForUser(email))
                .thenThrow(new IllegalArgumentException("User not found."));
        mockMvc.perform(post("/api/users/my/bonus-card")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found."));
        verify(bonusCardService, times(1)).createForUser(email);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateForCurrentUser_InvalidInput() throws Exception {
        String email = "user@example.com";
        var user = new cz.cvut.fit.tjv.online_store.domain.User();
        user.setId(1L);
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bonusCardService.createForUser(email))
                .thenThrow(new IllegalArgumentException("Invalid input data."));
        mockMvc.perform(post("/api/users/my/bonus-card")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"balance\":-100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid input data."));

        verify(bonusCardService, times(1)).createForUser(email);
    }

}