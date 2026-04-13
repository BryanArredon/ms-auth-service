package com.security.auth_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import jakarta.servlet.Filter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Order(1)
public class WebSecurityConfig {

    @Autowired
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer corsConfigurer() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                            "http://localhost:3000", 
                            "http://localhost:5000", 
                            "http://localhost:8000", 
                            "https://bitacoraenfermeria.com", 
                            "https://app.bitacoraenfermeria.com", 
                            "https://ms-auth.bitacoraenfermeria.com", 
                            "https://api.bitacoraenfermeria.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()) // Integra la configuración de CORS anterior
            .csrf(csrf -> csrf.disable())      // Deshabilita CSRF (Seguro para APIs Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas de autenticación y documentación
                .requestMatchers(
                    "/api/auth/register", 
                    "/api/auth/login", 
                    "/api/auth/verify-mfa", 
                    "/api/auth/logout", 
                    "/api/auth/forgot-password", 
                    "/api/auth/validate-reset-token", 
                    "/api/auth/reset-password",
                    "/api/auth/mfa/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**", 
                    "/v3/api-docs/**"
                ).permitAll()
                
                // Rutas administrativas protegidas por rol ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Cualquier otra ruta requiere estar autenticado
                .anyRequest().authenticated()
            ) 
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}