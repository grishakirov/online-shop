package cz.cvut.fit.tjv.online_store.client.controller;

import cz.cvut.fit.tjv.online_store.client.controller.dto.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
public class OrderController {

    private final RestTemplate restTemplate;

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/orders")
    public String showOrders(Model model) {
        ResponseEntity<OrderDto[]> response = restTemplate.getForEntity("http://localhost:8081/orders", OrderDto[].class);
        List<OrderDto> orders = Arrays.asList(response.getBody());
        model.addAttribute("orders", orders);
        return "order";
    }
}