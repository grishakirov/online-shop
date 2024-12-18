package cz.cvut.fit.tjv.online_store.controller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BonusCardDto {
    private Long id;
    private Long userId;
    private Double balance;
}
