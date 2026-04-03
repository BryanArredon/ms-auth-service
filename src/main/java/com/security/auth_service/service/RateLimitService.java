package com.security.auth_service.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;
import com.security.auth_service.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Rate Limiting para proteger contra ataques DDoS
 * Limita los intentos de login a X por minuto por IP
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final RateLimitProperties rateLimitProperties;

    /**
     * Obtiene o crea un bucket para una IP específica
     */
    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, key -> createNewBucket());
    }

    /**
     * Crea un nuevo bucket con los límites configurados
     */
    private Bucket createNewBucket() {
        int maxRequests = rateLimitProperties.getMaxRequests();
        Duration duration = Duration.ofMinutes(rateLimitProperties.getWindowMinutes());
        Bandwidth limit = Bandwidth.classic(maxRequests, Refill.intervally(maxRequests, duration));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Verifica si se permite la solicitud para una IP
     * @param ip Dirección IP del cliente
     * @return true si se permite, false si se ha excedido el límite
     */
    public boolean allowRequest(String ip) {
        Bucket bucket = resolveBucket(ip);
        return bucket.tryConsume(1);
    }

    /**
     * Obtiene los tokens disponibles para una IP
     */
    public long getAvailableTokens(String ip) {
        Bucket bucket = resolveBucket(ip);
        return bucket.getAvailableTokens();
    }

    /**
     * Limpia la caché de buckets (útil para testing)
     */
    public void clearCache() {
        cache.clear();
    }
}
