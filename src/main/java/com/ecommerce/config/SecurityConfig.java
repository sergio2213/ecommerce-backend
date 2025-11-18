package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ecommerce.security.JpaUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JpaUserDetailsService jpaUserDetailsService;

    public SecurityConfig(JpaUserDetailsService jpaUserDetailsService) {
        this.jpaUserDetailsService = jpaUserDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(
                authorize -> authorize
                    // endpoints de admin
                    .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                    // endpoints públicos (no requieren autenticación)
                    .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                    .requestMatchers("/h2-console/**").permitAll() // solo para desarrollo
                    // endpoints de usuario (requieren autenticación)
                    .requestMatchers(HttpMethod.GET, "/api/products").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/carts").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/carts/add-product").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/orders/checkout").authenticated()
                    // cualquier otra solicitud requiere autenticación
                    .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(t -> t.disable()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(jpaUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}
