package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.ProductDto;
import cz.cvut.fit.tjv.online_store.domain.Product;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductDto save(ProductDto productDto) {
        if (productDto.getIsRestricted() == null) {
            productDto.setIsRestricted(false);
        }
        Product product = productMapper.convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.convertToDto(savedProduct);
    }

    public Iterable<ProductDto> findAll() {
        List<Product> products = (List<Product>) productRepository.findAll();
        return productMapper.converManyToDto(products);
    }

    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return productMapper.convertToDto(product);
    }

    public ProductDto update(Long id, ProductDto productDto) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        Product product = productMapper.convertToEntity(productDto);
        product.setId(id);
        Product updatedProduct = productRepository.save(product);
        return productMapper.convertToDto(updatedProduct);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }
}