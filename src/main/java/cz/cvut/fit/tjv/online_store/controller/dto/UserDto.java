package cz.cvut.fit.tjv.online_store.controller.dto;

import cz.cvut.fit.tjv.online_store.domain.Role;
import lombok.*;

@Getter
@Setter

public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private Role role;
}
