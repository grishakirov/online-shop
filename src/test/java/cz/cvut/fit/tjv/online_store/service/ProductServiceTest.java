package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.ProductDto;
import cz.cvut.fit.tjv.online_store.domain.Product;
import cz.cvut.fit.tjv.online_store.repository.ProductRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Product product = new Product(1L, "Product1", 100.0, 10, false, null);
        ProductDto productDto = new ProductDto(1L, "Product1", 100.0, 10, false, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.convertToDto(product)).thenReturn(productDto);

        ProductDto result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Product1", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveProduct() {
        Product product = new Product(1L, "Product1", 100.0, 10, false, null);
        ProductDto productDto = new ProductDto(1L, "Product1", 100.0, 10, false, null);

        when(productMapper.convertToEntity(productDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.convertToDto(product)).thenReturn(productDto);

        ProductDto result = productService.save(productDto);

        assertNotNull(result);
        assertEquals("Product1", result.getName());
        verify(productRepository, times(1)).save(product);
    }
}