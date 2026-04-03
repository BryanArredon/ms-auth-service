package com.security.auth_service.config;

import com.security.auth_service.utils.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC para registrar interceptores
 * Incluye el interceptor de Rate Limiting
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar el interceptor de Rate Limiting para toda la aplicación
        registry.addInterceptor(rateLimitInterceptor);
    }
}
