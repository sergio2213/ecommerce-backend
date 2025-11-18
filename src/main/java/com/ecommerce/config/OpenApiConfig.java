package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("API Backend E-commerce (Spring Boot)")
                .version("v1.0")
                .description("Documentación completa de los servicios core (Auth, Productos, Carrito, Pedido).")
            )
            .addSecurityItem(new SecurityRequirement().addList("basicScheme"))
            .components(new Components()
                .addSecuritySchemes("basicScheme", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("Se requiere autenticación básica para la mayoría de los endpoints.")
                )    
            );
    }
}
