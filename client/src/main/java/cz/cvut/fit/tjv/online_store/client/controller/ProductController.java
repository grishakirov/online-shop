package cz.cvut.fit.tjv.online_store.client.controller;

import cz.cvut.fit.tjv.online_store.client.controller.dto.ProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
public class ProductController {
    private final RestTemplate restTemplate;

    public ProductController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/products")
    public String showProducts(Model model) {
        ResponseEntity<ProductDto[]> response = restTemplate.getForEntity("http://localhost:8081/products", ProductDto[].class);
        List<ProductDto> products = Arrays.asList(response.getBody());
        model.addAttribute("products", products);
        return "products";
    }
}