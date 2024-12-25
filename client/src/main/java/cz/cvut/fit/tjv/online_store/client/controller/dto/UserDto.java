package cz.cvut.fit.tjv.online_store.client.controller.dto;

import cz.cvut.fit.tjv.online_store.client.domain.Role;
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
    private String email;
    private String password;
    private Role role;
    private LocalDate birthDate;
}
