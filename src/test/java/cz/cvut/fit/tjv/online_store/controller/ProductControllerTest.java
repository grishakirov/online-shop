package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.ProductDto;
import cz.cvut.fit.tjv.online_store.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void testGetAllProducts() throws Exception {
        ProductDto product1 = new ProductDto(1L, "Product1", 100.0, 10, false, 18);
        ProductDto product2 = new ProductDto(2L, "Product2", 200.0, 20, true, null);

        when(productService.findAll()).thenReturn(Arrays.asList(product1, product2));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product2"));

        verify(productService, times(1)).findAll();
    }

    @Test
    void testGetProductById() throws Exception {
        ProductDto product = new ProductDto(1L, "Product1", 100.0, 10, false, 18);

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product1"));

        verify(productService, times(1)).findById(1L);
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductDto savedProduct = new ProductDto(1L, "Product1", 100.0, 10, false, 18);

        when(productService.save(any())).thenReturn(savedProduct);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Product1\",\"price\":100.0,\"quantity\":10,\"isRestricted\":false,\"allowedAge\":18}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product1"));

        verify(productService, times(1)).save(any());
    }

    @Test
    void testUpdateProduct() throws Exception {
        ProductDto updatedProduct = new ProductDto(1L, "UpdatedProduct", 150.0, 15, true, 21);

        when(productService.update(eq(1L), any())).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdatedProduct\",\"price\":150.0,\"quantity\":15,\"isRestricted\":true,\"allowedAge\":21}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedProduct"));

        verify(productService, times(1)).update(eq(1L), any());
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).delete(1L);
    }
}