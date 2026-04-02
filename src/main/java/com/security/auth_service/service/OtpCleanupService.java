package com.security.auth_service.service;

import com.security.auth_service.repository.MfaOtpeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupService {

    private final MfaOtpeRepository mfaOtpeRepository;

    // Se ejecuta automáticamente cada 1 minuto
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void limpiarOtpsExpirados() {
        try {
            mfaOtpeRepository.deleteByExpiraEnBefore(LocalDateTime.now());
            // log.info("Se han limpiado los códigos OTP expirados.");
        } catch (Exception e) {
            log.error("Error al limpiar los OTP expirados: " + e.getMessage());
        }
    }
}
