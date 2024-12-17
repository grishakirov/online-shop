package cz.cvut.fit.tjv.online_store.client.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import cz.cvut.fit.tjv.online_store.client.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.client.service.BonusCardService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class BonusCardController {

    @Autowired
    private BonusCardService bonusCardService;

    @GetMapping("/bonus-cards")
    public Mono<String> getBonusCards(Model model) {
        return bonusCardService.getAll()
                .collectList()
                .doOnNext(bonusCards -> model.addAttribute("bonusCards", bonusCards))
                .thenReturn("bonus-cards");
    }
}