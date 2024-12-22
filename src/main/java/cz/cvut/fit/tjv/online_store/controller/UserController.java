package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.Role;
import cz.cvut.fit.tjv.online_store.exception.ValidationException;
import cz.cvut.fit.tjv.online_store.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their unique ID. Admin-only access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public UserDto get(@Parameter(description = "ID of the user to retrieve") @PathVariable("id") Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users. Admin-only access.")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users")
    })
    @GetMapping
    public Iterable<UserDto> getAll() {
        return userService.findAll();
    }

    @Operation(summary = "Create a new user (Registration)", description = "Register a new user. Defaults to CUSTOMER role if no role is provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/registr")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid UserDto userDto) {
        if (userDto.getRole() == null) {
            userDto.setRole(Role.CUSTOMER);
        }
        return userService.save(userDto);
    }

    @Operation(summary = "Update user by ID", description = "Update details of an existing user by their unique ID. Admin-only access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PatchMapping("/{id}")
    public UserDto update(@Parameter(description = "ID of the user to update") @PathVariable("id") Long id,
                          @RequestBody UserDto userDto) {
        return userService.update(id, userDto);
    }

    @Operation(summary = "Delete a user by ID", description = "Delete a user by their unique ID. Optionally checks for active orders before deletion. Admin-only access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid deletion attempt due to active orders"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable("id") Long id,
            @Parameter(description = "Flag to check for active orders before deletion")
            @RequestParam(value = "with-check", defaultValue = "true") boolean withCheck) {
        if (withCheck) {
            userService.deleteUserIfNoActiveOrders(id);
        } else {
            userService.delete(id);
        }
    }

    @Operation(summary = "Get authenticated user", description = "Retrieve details of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authenticated user"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping("/authenticated")

    public UserDto getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Unauthorized access. User is not authenticated.");
        }

        String email = authentication.getName();
        UserDto user = userService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        System.out.println("Authenticated User: " + user);
        return user;
    }
}