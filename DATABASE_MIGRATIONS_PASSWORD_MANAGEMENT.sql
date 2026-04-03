-- ================================================================
-- MIGRACIONES SQL - GESTIÓN COMPLETA DE CONTRASEÑAS
-- Schema: seguridad_ms
-- ================================================================

-- 1. AGREGAR CAMPOS A TABLA usuarios
ALTER TABLE seguridad_ms.usuarios
ADD COLUMN password_expirado_en TIMESTAMP NULL COMMENT 'Fecha de expiración de la contraseña (90 días)';

-- 2. CREAR TABLA password_history
-- Almacena el historial de contraseñas para evitar reutilización
CREATE TABLE seguridad_ms.password_history (
    id UUID PRIMARY KEY DEFAULT UUID(),
    usuario_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES seguridad_ms.usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_fecha_cambio (fecha_cambio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de contraseñas de usuarios para evitar reutilización';

-- 3. CREAR TABLA password_reset_tokens
-- Almacena tokens de recuperación de contraseña temporal
CREATE TABLE seguridad_ms.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT UUID(),
    usuario_id UUID NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    estirado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (usuario_id) REFERENCES seguridad_ms.usuarios(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_fecha_expiracion (fecha_expiracion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tokens temporales para recuperación de contraseña (válidos 15 minutos)';

-- ================================================================
-- INDICES PARA OPTIMIZACIÓN
-- ================================================================

-- Índice para búsquedas rápidas de historial por usuario
CREATE INDEX idx_password_history_usuario_fecha 
ON seguridad_ms.password_history(usuario_id, fecha_cambio DESC);

-- Índice para búsquedas rápidas de tokens válidos
CREATE INDEX idx_reset_tokens_validos 
ON seguridad_ms.password_reset_tokens(usuario_id, estirado, fecha_expiracion);
