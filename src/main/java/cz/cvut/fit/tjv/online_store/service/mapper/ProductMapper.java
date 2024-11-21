package cz.cvut.fit.tjv.online_store.service.mapper;

import cz.cvut.fit.tjv.online_store.controller.dto.ProductDto;
import cz.cvut.fit.tjv.online_store.domain.Product;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper implements CustomMapper<Product, ProductDto> {
    private final ModelMapper modelMapper;

    public ProductMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDto convertToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public Product convertToEntity(ProductDto productDto) {
        return modelMapper.map(productDto, Product.class);
    }

    @Override
    public List<ProductDto> converManyToDto(List<Product> products) {
        return products.stream()
                .map(this::convertToDto)
                .toList();
    }
}
