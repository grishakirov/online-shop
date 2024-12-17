package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/users")
    public String showUsersPage() {
        return "users";
    }
}