package cz.cvut.fit.tjv.online_store.service.mapper;

import cz.cvut.fit.tjv.online_store.controller.dto.OrderDto;
import cz.cvut.fit.tjv.online_store.domain.Order;
import cz.cvut.fit.tjv.online_store.domain.Product;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;

    public OrderMapper(ModelMapper modelMapper, ProductRepository productRepository) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    public OrderDto convertToDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setProductIds(order.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList()));
        return orderDto;
    }

    public Order convertToEntity(OrderDto orderDto) {
        Order order = modelMapper.map(orderDto, Order.class);
        List<Product> products = (List<Product>) productRepository.findAllById(orderDto.getProductIds());
        order.setProducts(products);
        return order;
    }

    public List<OrderDto> converManyToDto(List<Order> orders) {
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }
}