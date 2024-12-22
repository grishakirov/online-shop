package cz.cvut.fit.tjv.online_store.controller.dto;

import cz.cvut.fit.tjv.online_store.domain.Role;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    @Email(message = "Invalid email format")
    private String email;
    private String password;
    private Role role;
    private LocalDate birthDate;
}
