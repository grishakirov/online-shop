package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/bonus-cards")
@Tag(name = "Bonus Cards", description = "Endpoints for managing bonus cards.")
public class BonusCardController {

    private final BonusCardService bonusCardService;
    private final UserRepository userRepository;
    private final BonusCardRepository bonusCardRepository;
    private final BonusCardMapper bonusCardMapper;

    public BonusCardController(BonusCardService bonusCardService, UserRepository userRepository, BonusCardRepository bonusCardRepository, BonusCardMapper bonusCardMapper) {
        this.bonusCardService = bonusCardService;
        this.userRepository = userRepository;
        this.bonusCardRepository = bonusCardRepository;
        this.bonusCardMapper = bonusCardMapper;
    }

    @Operation(summary = "Get all bonus cards", description = "Retrieve a list of all bonus cards. Requires administrator privileges.")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bonus cards"),
            @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    @GetMapping
    public List<BonusCardDto> getAllBonusCards() {
        return bonusCardService.findAll();
    }

    @Operation(summary = "Get bonus card by ID", description = "Retrieve a specific bonus card by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bonus card"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    @GetMapping("/{id}")
    public BonusCardDto getBonusCardById(
            @Parameter(description = "ID of the bonus card to retrieve") @PathVariable Long id) {
        return bonusCardService.findById(id);
    }

    @Operation(summary = "Create a new bonus card", description = "Create a new bonus card associated with a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bonus card successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BonusCardDto createBonusCard(@RequestBody BonusCardDto bonusCardDto) {
        return bonusCardService.save(bonusCardDto);
    }

    @Operation(summary = "Delete bonus card by ID", description = "Delete a specific bonus card by its unique ID. Requires administrator privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bonus card successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found"),
            @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBonusCard(
            @Parameter(description = "ID of the bonus card to delete") @PathVariable Long id) {
        bonusCardService.delete(id);
    }

    @Operation(summary = "Add balance to a bonus card", description = "Add a specified amount to the balance of a bonus card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added balance to bonus card"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    @PostMapping("/{id}/add-balance")
    public BonusCardDto addBalance(
            @Parameter(description = "ID of the bonus card") @PathVariable Long id,
            @Parameter(description = "Amount to add to the balance") @RequestParam Double amount) {
        return bonusCardService.addBalance(id, amount);
    }

    @Operation(summary = "Deduct balance from a bonus card", description = "Deduct a specified amount from the balance of a bonus card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deducted balance from bonus card"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance")
    })
    @PostMapping("/{id}/deduct-balance")
    public BonusCardDto deductBalance(
            @Parameter(description = "ID of the bonus card") @PathVariable Long id,
            @Parameter(description = "Amount to deduct from the balance") @RequestParam Double amount) {
        return bonusCardService.deductBalance(id, amount);
    }

    @Operation(summary = "Get current user's bonus card", description = "Retrieve the authenticated user's bonus card. If the user doesn't have a card, one is created automatically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved or created user's bonus card"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/me")
    public BonusCardDto getCurrentUserBonusCard(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(user -> bonusCardRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            // Automatically create a new BonusCard for the user if none exists
                            return bonusCardMapper.convertToEntity(bonusCardService.createForUser(email));
                        }))
                .map(bonusCardMapper::convertToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    @Operation(summary = "Create bonus card for the current user", description = "Create a bonus card for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bonus card successfully created"),
            @ApiResponse(responseCode = "400", description = "User already has a bonus card"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    public BonusCardDto createForCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return bonusCardService.createForUser(email);
    }
}