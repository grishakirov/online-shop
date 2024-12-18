package cz.cvut.fit.tjv.online_store.configuration;

import cz.cvut.fit.tjv.online_store.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfiguration {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfiguration(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/users/registr").permitAll()

                        .requestMatchers("/users/**").hasRole("ADMINISTRATOR")

                        .requestMatchers("/bonus-cards/my").hasRole("CUSTOMER")
                        .requestMatchers("/bonus-cards/**").hasRole("ADMINISTRATOR")

                        .requestMatchers("/orders/my/**").hasRole("CUSTOMER")
                        .requestMatchers("/orders/**").hasRole("ADMINISTRATOR")

                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMINISTRATOR")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("http://localhost:8082/", true)
                        .failureUrl("http://localhost:8082/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("http://localhost:8082/login?logout")
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}