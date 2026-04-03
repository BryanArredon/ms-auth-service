package com.security.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Propiedades configurables de Rate Limiting
 * Pueden ser modificadas en application.properties o application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    
    /**
     * Número máximo de intentos permitidos por ventana de tiempo
     * Default: 10 intentos por minuto
     */
    private int maxRequests = 10;
    
    /**
     * Ventana de tiempo en minutos para el reset de intentos
     * Default: 1 minuto
     */
    private int windowMinutes = 1;
    
    /**
     * Enable/Disable rate limiting
     * Default: true
     */
    private boolean enabled = true;
}
