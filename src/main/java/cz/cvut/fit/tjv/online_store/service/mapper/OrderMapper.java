package cz.cvut.fit.tjv.online_store.service.mapper;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    private final ModelMapper modelMapper;

    public OrderMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public OrderDto convertToDto(Order order) {
        if (order == null) return null;

        return modelMapper.map(order, OrderDto.class);
    }

    public Order convertToEntity(OrderDto orderDto) {
        if (orderDto == null) return null;

        Order order = modelMapper.map(orderDto, Order.class);
        if (orderDto.getRequestedQuantities() != null) {
            order.setRequestedQuantities(orderDto.getRequestedQuantities());
        }
        return order;
    }

    public List<OrderDto> convertManyToDto(List<Order> orders) {
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

}