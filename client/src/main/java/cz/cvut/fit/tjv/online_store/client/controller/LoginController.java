package cz.cvut.fit.tjv.online_store.client.controller;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class LoginController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final WebSessionServerSecurityContextRepository contextRepository = new WebSessionServerSecurityContextRepository();

    public LoginController(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> login(@RequestParam String username, @RequestParam String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))
                .doOnNext(auth -> {
                    SecurityContext securityContext = new SecurityContextImpl(auth);
                    contextRepository.save(null, securityContext);
                })
                .map(Authentication::getName)
                .map(name -> "Welcome " + name + "!");
    }


}