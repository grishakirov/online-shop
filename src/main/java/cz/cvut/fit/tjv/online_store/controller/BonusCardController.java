package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bonus-cards")
public class BonusCardController {

    private final BonusCardService bonusCardService;

    public BonusCardController(BonusCardService bonusCardService) {
        this.bonusCardService = bonusCardService;
    }

    @Operation(summary = "Get all bonus cards", description = "Retrieve a list of all bonus cards.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bonus cards")
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
    public BonusCardDto getBonusCardById(@Parameter(description = "ID of the bonus card to retrieve") @PathVariable Long id) {
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

    @Operation(summary = "Delete bonus card by ID", description = "Delete a specific bonus card by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bonus card successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBonusCard(@Parameter(description = "ID of the bonus card to delete") @PathVariable Long id) {
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

    @Operation(summary = "Create bonus card for the current user", description = "Create a bonus card for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bonus card successfully created"),
            @ApiResponse(responseCode = "400", description = "User already has a bonus card")
    })
    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    public BonusCardDto createForCurrentUser(@Parameter(description = "Authentication object representing the current user") Authentication authentication) {
        String email = authentication.getName();
        return bonusCardService.createForUser(email);
    }
}