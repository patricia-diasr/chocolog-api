package com.chocolog.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

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
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(toH2Console()).permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/flavors").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/flavors/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/flavors/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/employees").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/employees").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/employees/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/employees/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/reports/dashboard").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                    "/customers", "/flavors", "/flavors/{id}", "/employees/{id}",
                    "/stock-records", "/orders", "/customers/{id}/orders",
                    "/customers/*/orders/*",
                    "/orders/items", "/orders/print-batchs",
                    "/orders/print-batchs/{id}", "/orders/print-batchs/{id}/download"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.POST,
                    "/customers", "/stock-records",
                    "/customers/*/orders/*",
                    "/customers/*/orders/*/items",
                    "/customers/*/orders/*/payments",
                    "/orders/print-batchs"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.PATCH,
                    "/customers",
                    "/customers/*/orders/*",
                    "/customers/*/orders/*/items/*",
                    "/customers/*/orders/*/payments/*"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(HttpMethod.DELETE,
                    "/customers/*/orders/*",
                    "/customers/*/orders/*/items/*",
                    "/customers/*/orders/*/payments/*"
                ).hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}