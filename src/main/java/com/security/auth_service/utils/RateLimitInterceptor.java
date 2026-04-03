package com.security.auth_service.utils;

import com.security.auth_service.service.RateLimitService;
import com.security.auth_service.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor que aplica Rate Limiting a las solicitudes
 * Protege endpoints contra ataques de fuerza bruta
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        // Verificar si rate limiting está habilitado
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }
        
        // Solo aplicar rate limiting a endpoints específicos
        String requestUri = request.getRequestURI();
        
        // Aplicar rate limiting a: login y forgot-password
        if (!isRateLimitedEndpoint(requestUri)) {
            return true;
        }

        // Obtener la IP del cliente
        String ip = getClientIp(request);

        // Verificar si se permite la solicitud
        if (!rateLimitService.allowRequest(ip)) {
            return handleRateLimitExceeded(response, ip);
        }

        return true;
    }

    /**
     * Verifica si el endpoint debe estar protegido por rate limiting
     */
    private boolean isRateLimitedEndpoint(String uri) {
        return uri.contains("/api/auth/login") || 
               uri.contains("/api/auth/forgot-password") ||
               uri.contains("/api/auth/verify-mfa");
    }

    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs en X-Forwarded-For, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Maneja el caso cuando se excede el límite de rate limiting
     */
    private boolean handleRateLimitExceeded(HttpServletResponse response, String ip) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
        response.setContentType("application/json");

        long timestamp = System.currentTimeMillis();
        String jsonResponse = String.format(
                "{\"error\":\"Too Many Requests\",\"message\":\"Ha excedido el límite de intentos permitidos. Intente más tarde.\",\"status\":429,\"timestamp\":%d}",
                timestamp
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        return false;
    }
}
