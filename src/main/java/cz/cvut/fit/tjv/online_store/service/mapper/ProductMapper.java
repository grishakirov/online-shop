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
        this.modelMapper.typeMap(ProductDto.class, Product.class).addMappings(mapper -> {
            mapper.skip(Product::setIsRestricted); // Skip automatic mapping for isRestricted
        });;
    }

    @Override
    public ProductDto convertToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public Product convertToEntity(ProductDto productDto) {
        Product product = modelMapper.map(productDto, Product.class);
        if (product.getIsRestricted() == null) {
            product.setIsRestricted(false); // Apply default value
        }
        return product;
    }

    @Override
    public List<ProductDto> converManyToDto(List<Product> products) {
        return products.stream()
                .map(this::convertToDto)
                .toList();
    }
}
