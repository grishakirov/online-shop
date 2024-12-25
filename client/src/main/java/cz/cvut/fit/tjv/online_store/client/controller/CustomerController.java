package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {

    @GetMapping("/personal-details")
    public String showPersonalDetailsPage() {
        return "personal_details";
    }

    @GetMapping("/products-customer")
    public String showProductsForCustomer() {
        return "products_customer";
    }

    @GetMapping("/order-recap")
    public String showOrderRecapPage() {
        return "order_recap";
    }

    @GetMapping("/cart")
    public String showCartPage() {
        return "cart";
    }

}