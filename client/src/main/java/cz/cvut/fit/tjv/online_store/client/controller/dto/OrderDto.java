package cz.cvut.fit.tjv.online_store.client.controller.dto;

import cz.cvut.fit.tjv.online_store.client.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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