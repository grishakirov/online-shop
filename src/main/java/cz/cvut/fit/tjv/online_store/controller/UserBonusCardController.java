package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users/my/bonus-card")
public class UserBonusCardController {

    private final BonusCardService bonusCardService;
    private final UserRepository userRepository;
    private final BonusCardRepository bonusCardRepository;
    private final BonusCardMapper bonusCardMapper;

    public UserBonusCardController(BonusCardService bonusCardService, UserRepository userRepository,
                                   BonusCardRepository bonusCardRepository, BonusCardMapper bonusCardMapper) {
        this.bonusCardService = bonusCardService;
        this.userRepository = userRepository;
        this.bonusCardRepository = bonusCardRepository;
        this.bonusCardMapper = bonusCardMapper;
    }

    @Operation(summary = "Get current user's bonus card", description = "Retrieve the authenticated user's bonus card.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's bonus card")
    @ApiResponse(responseCode = "404", description = "Bonus card not found")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @GetMapping
    public BonusCardDto getCurrentUserBonusCard(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> bonusCardRepository.findByUserId(user.getId()))
                .map(optionalCard -> optionalCard
                        .map(bonusCardMapper::convertToDto)
                        .orElseThrow(() -> new IllegalArgumentException("Bonus card not found.")))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    @Operation(summary = "Create bonus card for the current user", description = "Create a bonus card for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Bonus card successfully created")
    @ApiResponse(responseCode = "400", description = "User already has a bonus card")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BonusCardDto createForCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("User is not authenticated.");
        }

        String email = authentication.getName();
        return bonusCardService.createForUser(email);
    }
}