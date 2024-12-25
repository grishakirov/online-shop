package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/users")
    public String showUsersPage() {
        return "users";
    }

    @GetMapping("/bonus-cards-admin")
    public String showBonusCardsPage() {
        return "bonus_cards";
    }

    @GetMapping("/products-admin")
    public String showProductsAdminPage() {
        return "products_admin";
    }

    @GetMapping("/orders-admin")
    public String showOrdersAdminPage() {
        return "orders_admin";
    }
}