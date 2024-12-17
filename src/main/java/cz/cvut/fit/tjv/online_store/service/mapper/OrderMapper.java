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
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        if (order.getRequestedQuantities() != null) {
            orderDto.setRequestedQuantities(order.getRequestedQuantities());
        }

        orderDto.setProductIds(order.getRequestedQuantities().keySet().stream().toList());

        return orderDto;
    }

    public Order convertToEntity(OrderDto orderDto) {
        if (orderDto == null) return null;

        Order order = new Order();
        order.setRequestedQuantities(orderDto.getRequestedQuantities());
        order.setDateOfCreation(orderDto.getDateOfCreation());
        order.setTotalCost(orderDto.getTotalCost());
        order.setStatus(orderDto.getStatus());
        return order;
    }

    public List<OrderDto> converManyToDto(List<Order> orders) {
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}