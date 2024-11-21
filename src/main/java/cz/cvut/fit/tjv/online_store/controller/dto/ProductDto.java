package cz.cvut.fit.tjv.online_store.controller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    private Boolean isRestricted;
    private Integer allowedAge;
}