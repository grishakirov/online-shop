package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.service.BonusCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bonus-cards")
public class BonusCardController {

    private final BonusCardService bonusCardService;

    public BonusCardController(BonusCardService bonusCardService) {
        this.bonusCardService = bonusCardService;
    }

    @Operation(summary = "Get all bonus cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bonus cards")
    })
    @GetMapping
    public List<BonusCardDto> getAllBonusCards() {
        return bonusCardService.findAll();
    }

    @Operation(summary = "Get bonus card by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bonus card"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    @GetMapping("/{id}")
    public BonusCardDto getBonusCardById(@PathVariable Long id) {
        return bonusCardService.findById(id);
    }

    @Operation(summary = "Create a new bonus card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bonus card successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BonusCardDto createBonusCard(@RequestBody BonusCardDto bonusCardDto) {
        return bonusCardService.save(bonusCardDto);
    }

    @Operation(summary = "Delete bonus card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bonus card successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBonusCard(@PathVariable Long id) {
        bonusCardService.delete(id);
    }

    @Operation(summary = "Find bonus card by card number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bonus card"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    @GetMapping("/find-by-card-number/{cardNumber}")
    public BonusCardDto findBonusCardByCardNumber(@PathVariable String cardNumber) {
        return bonusCardService.findByCardNumber(cardNumber);
    }

    @Operation(summary = "Add balance to a bonus card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added balance"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    @PostMapping("/{id}/add-balance")
    public BonusCardDto addBalance(@PathVariable Long id, @RequestParam Double amount) {
        return bonusCardService.addBalance(id, amount);
    }

    @Operation(summary = "Deduct balance from a bonus card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deducted balance"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance")
    })
    @PostMapping("/{id}/deduct-balance")
    public BonusCardDto deductBalance(@PathVariable Long id, @RequestParam Double amount) {
        return bonusCardService.deductBalance(id, amount);
    }
}