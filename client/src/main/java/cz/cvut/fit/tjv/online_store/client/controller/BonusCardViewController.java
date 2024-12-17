package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BonusCardViewController {

    @GetMapping("/bonus-cards")
    public String showBonusCardsPage() {
        return "bonus-cards";
    }
}
