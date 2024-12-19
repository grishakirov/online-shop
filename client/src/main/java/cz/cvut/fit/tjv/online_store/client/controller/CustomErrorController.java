package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "error/404";
    }

    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}