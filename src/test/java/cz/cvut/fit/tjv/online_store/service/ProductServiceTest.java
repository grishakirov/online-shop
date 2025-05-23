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
    void shouldFindProductByIdSuccessfully() {
        Product product = new Product(1L, "Product1", 100.0, 10, false, null);
        ProductDto productDto = new ProductDto(1L, "Product1", 100.0, 10, false, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.convertToDto(product)).thenReturn(productDto);

        ProductDto result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Product1", result.getName());
        verify(productRepository).findById(1L);
        verify(productMapper).convertToDto(product);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.findById(1L)
        );
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findById(1L);
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldSaveProductSuccessfully() {
        Product productEntity = new Product(1L, "Product1", 100.0, 10, false, null);
        ProductDto productDto = new ProductDto(1L, "Product1", 100.0, 10, false, null);

        when(productMapper.convertToEntity(productDto)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(productMapper.convertToDto(productEntity)).thenReturn(productDto);

        ProductDto result = productService.save(productDto);

        assertNotNull(result);
        assertEquals("Product1", result.getName());
        verify(productMapper).convertToEntity(productDto);
        verify(productRepository).save(productEntity);
        verify(productMapper).convertToDto(productEntity);
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Long productId = 1L;
        ProductDto updatedProductDto =
                new ProductDto(productId, "UpdatedProduct", 150.0, 5, false, null);
        Product updatedProductEntity =
                new Product(productId, "UpdatedProduct", 150.0, 5, false, null);

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productMapper.convertToEntity(updatedProductDto)).thenReturn(updatedProductEntity);
        when(productRepository.save(updatedProductEntity)).thenReturn(updatedProductEntity);
        when(productMapper.convertToDto(updatedProductEntity)).thenReturn(updatedProductDto);

        ProductDto result = productService.update(productId, updatedProductDto);

        assertNotNull(result);
        assertEquals("UpdatedProduct", result.getName());
        assertEquals(150.0, result.getPrice());
        assertEquals(5, result.getQuantity());
        assertFalse(result.getIsRestricted());

        verify(productRepository, times(1)).existsById(productId);
        verify(productMapper, times(1)).convertToEntity(updatedProductDto);
        verify(productRepository, times(1)).save(updatedProductEntity);
        verify(productMapper, times(1)).convertToDto(updatedProductEntity);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        Long productId = 1L;
        ProductDto updatedProductDto =
                new ProductDto(productId, "UpdatedProduct", 150.0, 5, false, null);

        when(productRepository.existsById(productId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.update(productId, updatedProductDto)
        );
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).existsById(productId);
        verifyNoInteractions(productMapper);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(true);

        productService.delete(productId);

        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.delete(productId)
        );
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(anyLong());
    }
}