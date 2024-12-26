package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bonus-cards")
public class BonusCardController {

    private final BonusCardService bonusCardService;

    public BonusCardController(BonusCardService bonusCardService) {
        this.bonusCardService = bonusCardService;
    }

    @Operation(summary = "Get all bonus cards", description = "Retrieve a list of all bonus cards. Requires administrator privileges.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bonus cards")
    @ApiResponse(responseCode = "403", description = "Access forbidden")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public List<BonusCardDto> getAllBonusCards() {
        return bonusCardService.findAll();
    }

    @Operation(summary = "Get bonus card by ID", description = "Retrieve a specific bonus card by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bonus card")
    @ApiResponse(responseCode = "404", description = "Bonus card not found")
    @GetMapping("/{id}")
    public BonusCardDto getBonusCardById(@PathVariable Long id) {
        return bonusCardService.findById(id);
    }

    @Operation(summary = "Create a new bonus card", description = "Create a new bonus card associated with a user.")
    @ApiResponse(responseCode = "201", description = "Bonus card successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
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
    public void deleteBonusCard(@PathVariable Long id) {
        bonusCardService.delete(id);
    }

    @Operation(summary = "Add balance to a bonus card", description = "Add a specified amount to the balance of a bonus card.")
    @ApiResponse(responseCode = "200", description = "Successfully added balance to bonus card")
    @ApiResponse(responseCode = "404", description = "Bonus card not found")
    @ApiResponse(responseCode = "400", description = "Invalid amount")
    @PostMapping("/{id}/add-balance")
    public BonusCardDto addBalance(@PathVariable Long id, @RequestParam Double amount) {
        return bonusCardService.addBalance(id, amount);
    }

    @Operation(summary = "Deduct balance from a bonus card", description = "Deduct a specified amount from the balance of a bonus card.")
    @ApiResponse(responseCode = "200", description = "Successfully deducted balance from bonus card")
    @ApiResponse(responseCode = "404", description = "Bonus card not found")
    @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance")
    @PostMapping("/{id}/deduct-balance")
    public BonusCardDto deductBalance(@PathVariable Long id, @RequestParam Double amount) {
        return bonusCardService.deductBalance(id, amount);
    }
}