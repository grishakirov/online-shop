package cz.cvut.fit.tjv.online_store.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${social_network_backend_url}")
    private String backendUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(backendUrl)
                .defaultCookie("SESSION", "your-session-cookie") // Use session cookie from Spring Security
                .build();
    }
}