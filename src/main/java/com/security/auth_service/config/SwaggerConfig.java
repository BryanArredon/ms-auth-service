package com.security.auth_service.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@Order(1)
public class SwaggerConfig {

@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .servers(List.of(
                new Server().url("https://ms-auth.bitacoraenfermeria.com").description("Servidor de Producción"),
                new Server().url("http://localhost:8085").description("Servidor Local")
            ))
            .info(new Info()
                    .title("API Microservicio de Autenticación")
                    .version("1.0")
                    .description("Documentación de los endpoints del microservicio de autenticación")
                    .license(new License().name("Apache 2.0").url("http://springdoc.org")))
            .components(new Components()
                    .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .in(SecurityScheme.In.HEADER)
                            .name("Authorization")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}   