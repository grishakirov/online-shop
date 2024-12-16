package cz.cvut.fit.tjv.online_store.controller.dto;

import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private Map<Long, Integer> requestedQuantities;
    private LocalDate dateOfCreation;
    private Double totalCost;
    private OrderStatus status;
    private List<Long> productIds;
}