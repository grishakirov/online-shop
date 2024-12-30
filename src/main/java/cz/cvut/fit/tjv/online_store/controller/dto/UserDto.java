package cz.cvut.fit.tjv.online_store.controller.dto;

import cz.cvut.fit.tjv.online_store.domain.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Name is required")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÿ]+(?:[ '-][A-Za-zÀ-ÿ]+)*$",
            message = "Name can only contain letters, spaces, hyphens, and apostrophes"
    )
    private String name;
    @NotBlank(message = "Name is required")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÿ]+(?:[ '-][A-Za-zÀ-ÿ]+)*$",
            message = "Surname can only contain letters, spaces, hyphens, and apostrophes"
    )
    private String surname;
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
    private Role role;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @PastOrPresent(message = "Birth date cannot be in the future")
    private LocalDate birthDate;

    public UserDto(Long id, String name, String surname, String email,
                   String password, Role role) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.birthDate = null;
    }
}
