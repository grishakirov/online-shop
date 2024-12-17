package cz.cvut.fit.tjv.online_store.client.service;

import cz.cvut.fit.tjv.online_store.client.controller.dto.BonusCardDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BonusCardService {

    private final WebClient webClient;

    public BonusCardService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<BonusCardDto> getAll() {
        return webClient.get()
                .uri("/bonus-cards")
                .retrieve()
                .bodyToFlux(BonusCardDto.class);
    }
}