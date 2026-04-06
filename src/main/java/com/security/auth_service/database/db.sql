-- Crear esquema de seguridad
CREATE SCHEMA IF NOT EXISTS seguridad_ms;

-- 1. Aplicaciones: Permite que este MS sirva a varios proyectos (Bitácora, Nómina, etc.)
CREATE TABLE seguridad_ms.aplicaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre TEXT UNIQUE NOT NULL,
    clave_api UUID DEFAULT uuid_generate_v4(),
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Usuarios: Solo credenciales básicas
CREATE TABLE seguridad_ms.usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    correo TEXT UNIQUE NOT NULL,
    password_hash TEXT NULL,
    proveedor_autenticacion VARCHAR(50) NULL,
    google_auth_secret VARCHAR(32) NULL,
    google_auth_activo BOOLEAN DEFAULT FALSE,
    cuenta_bloqueada BOOLEAN DEFAULT FALSE,
    intentos_fallidos INTEGER DEFAULT 0,
    ultimo_acceso TIMESTAMPTZ,
    fecha_creacion TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Roles: Definidos por aplicación
CREATE TABLE seguridad_ms.roles (
    id SERIAL PRIMARY KEY,
    aplicacion_id UUID REFERENCES seguridad_ms.aplicaciones(id),
    nombre_rol TEXT NOT NULL, 
    UNIQUE(aplicacion_id, nombre_rol)
);

-- 4. Asignación: Vincula usuarios con roles y apps
CREATE TABLE seguridad_ms.usuario_roles (
    usuario_id UUID REFERENCES seguridad_ms.usuarios(id),
    rol_id INTEGER REFERENCES seguridad_ms.roles(id),
    PRIMARY KEY (usuario_id, rol_id)
);


-- 5. Auditoria: Registro detallado de acciones sensibles
CREATE TABLE seguridad_ms.auditoria (
    id SERIAL PRIMARY KEY,
    usuario_id UUID REFERENCES seguridad_ms.usuarios(id),
    aplicacion_id UUID REFERENCES seguridad_ms.aplicaciones(id),
    accion TEXT NOT NULL,           
    tabla_afectada TEXT,           
    dato_id TEXT,                  
    valor_anterior JSONB,          
    valor_nuevo JSONB,                             
    fecha_accion TIMESTAMPTZ DEFAULT NOW()
);


-- 6. Historial de sesiones: Control de conexiones activas y pasadas
CREATE TABLE seguridad_ms.historial_sesiones (
    id SERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES seguridad_ms.usuarios(id),
    aplicacion_id UUID REFERENCES seguridad_ms.aplicaciones(id),
    rol_id INTEGER REFERENCES seguridad_ms.roles(id),
    fecha_inicio TIMESTAMPTZ DEFAULT NOW(),
    fecha_fin TIMESTAMPTZ,
    ip_origen INET,
    user_agent TEXT,               
    token TEXT UNIQUE,       
    estado VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'CERRADA', 'EXPIRADA', 'BLOQUEADA'))
);


CREATE TABLE seguridad_ms.mfa_otps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES seguridad_ms.usuarios(id) ON DELETE CASCADE,
    codigo VARCHAR(6) NOT NULL,
    expira_en TIMESTAMPTZ NOT NULL,
    fecha_creacion TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_mfa_usuario UNIQUE (usuario_id)
);

-- 7. Gestión de contraseñas: expiración, historial y reset tokens
ALTER TABLE seguridad_ms.usuarios
ADD COLUMN password_expirado_en TIMESTAMPTZ NULL;

CREATE TABLE seguridad_ms.password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    fecha_cambio TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_password_history_usuario
      FOREIGN KEY (usuario_id) REFERENCES seguridad_ms.usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_history_usuario_id
  ON seguridad_ms.password_history(usuario_id);

CREATE INDEX idx_password_history_fecha_cambio
  ON seguridad_ms.password_history(fecha_cambio DESC);

CREATE TABLE seguridad_ms.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    token TEXT NOT NULL UNIQUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_expiracion TIMESTAMPTZ NOT NULL,
    estirado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_password_reset_usuario
      FOREIGN KEY (usuario_id) REFERENCES seguridad_ms.usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token
  ON seguridad_ms.password_reset_tokens(token);

CREATE INDEX idx_password_reset_usuario_id
  ON seguridad_ms.password_reset_tokens(usuario_id);

CREATE INDEX idx_password_reset_expiracion
  ON seguridad_ms.password_reset_tokens(fecha_expiracion);


create extension if not exists pgcrypto;

create table seguridad_ms.refresh_tokens (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null,
    token text not null unique,
    fecha_creacion timestamptz not null default now(),
    fecha_expiracion timestamptz not null,
    revocado boolean not null default false,
    constraint fk_refresh_token_usuario
        foreign key (usuario_id)
        references seguridad_ms.usuarios(id)
        on delete cascade
);

create index idx_refresh_tokens_usuario_id
    on seguridad_ms.refresh_tokens(usuario_id);

COMMENT ON SCHEMA seguridad_ms IS 'Microservicio encargado de autenticación, autorización y control de acceso de usuarios para diferentes aplicaciones';

-- TABLA: aplicaciones
COMMENT ON TABLE seguridad_ms.aplicaciones IS 'Registro de aplicaciones que utilizan el microservicio de seguridad';

COMMENT ON COLUMN seguridad_ms.aplicaciones.id IS 'Identificador único de la aplicación generado mediante UUID';
COMMENT ON COLUMN seguridad_ms.aplicaciones.nombre IS 'Nombre único de la aplicación que consume el microservicio (ejemplo: BITACORA_ENFERMERIA)';
COMMENT ON COLUMN seguridad_ms.aplicaciones.clave_api IS 'Clave API utilizada para autenticar la comunicación entre la aplicación cliente y el microservicio de seguridad';
COMMENT ON COLUMN seguridad_ms.aplicaciones.activa IS 'Indica si la aplicación está habilitada para utilizar el sistema de autenticación';
COMMENT ON COLUMN seguridad_ms.aplicaciones.fecha_creacion IS 'Fecha y hora en que se registró la aplicación en el sistema';

-- TABLA: usuarios
COMMENT ON TABLE seguridad_ms.usuarios IS 'Usuarios registrados en el sistema con credenciales básicas de acceso';

COMMENT ON COLUMN seguridad_ms.usuarios.id IS 'Identificador único del usuario generado mediante UUID';
COMMENT ON COLUMN seguridad_ms.usuarios.correo IS 'Correo electrónico del usuario utilizado como identificador de inicio de sesión';
COMMENT ON COLUMN seguridad_ms.usuarios.password_hash IS 'Contraseña del usuario almacenada de forma segura mediante hash';
COMMENT ON COLUMN seguridad_ms.usuarios.proveedor_autenticacion IS 'Proveedor de autenticación utilizado por el usuario';
COMMENT ON COLUMN seguridad_ms.usuarios.cuenta_bloqueada IS 'Indica si la cuenta del usuario está bloqueada por seguridad o administración';
COMMENT ON COLUMN seguridad_ms.usuarios.intentos_fallidos IS 'Número de intentos de inicio fallidos seguidos, se bloquea al llegar a 5';
COMMENT ON COLUMN seguridad_ms.usuarios.ultimo_acceso IS 'Fecha y hora del último inicio de sesión exitoso del usuario';
COMMENT ON COLUMN seguridad_ms.usuarios.fecha_creacion IS 'Fecha y hora en que el usuario fue registrado en el sistema';

-- TABLA: roles
COMMENT ON TABLE seguridad_ms.roles IS 'Roles de acceso definidos para cada aplicación del sistema';

COMMENT ON COLUMN seguridad_ms.roles.id IS 'Identificador único del rol';
COMMENT ON COLUMN seguridad_ms.roles.aplicacion_id IS 'Referencia a la aplicación a la que pertenece el rol';
COMMENT ON COLUMN seguridad_ms.roles.nombre_rol IS 'Nombre del rol asignado dentro de la aplicación (por ejemplo: ENFERMERO, ADMIN, SUPERVISOR)';

-- TABLA: usuario_roles
COMMENT ON TABLE seguridad_ms.usuario_roles IS 'Tabla de relación que asigna roles a usuarios dentro del sistema';

COMMENT ON COLUMN seguridad_ms.usuario_roles.usuario_id IS 'Referencia al usuario al que se le asigna el rol';
COMMENT ON COLUMN seguridad_ms.usuario_roles.rol_id IS 'Referencia al rol asignado al usuario';


-- Comentarios para Auditoría
COMMENT ON TABLE seguridad_ms.auditoria IS 'Registro de cambios y acciones críticas realizadas por los usuarios';

COMMENT ON COLUMN seguridad_ms.auditoria.usuario_id IS 'Referencia al usuario que realizó la acción';
COMMENT ON COLUMN seguridad_ms.auditoria.aplicacion_id IS 'Referencia a la aplicación a la que pertenece el usuario';
COMMENT ON COLUMN seguridad_ms.auditoria.accion IS 'Tipo de operación realizada (CREATE, UPDATE, DELETE, etc.)';
COMMENT ON COLUMN seguridad_ms.auditoria.tabla_afectada IS 'Tabla que fue afectada por la acción';
COMMENT ON COLUMN seguridad_ms.auditoria.dato_id IS 'ID del registro que fue afectado';
COMMENT ON COLUMN seguridad_ms.auditoria.valor_anterior IS 'Estado del registro en formato JSON antes de la modificación';
COMMENT ON COLUMN seguridad_ms.auditoria.valor_nuevo IS 'Estado del registro en formato JSON después de la modificación';
COMMENT ON COLUMN seguridad_ms.auditoria.fecha_accion IS 'Fecha y hora en que se realizó la acción';


-- Comentarios para Historial de Sesiones
COMMENT ON TABLE seguridad_ms.historial_sesiones IS 'Registro de inicios y cierres de sesión para control de accesos simultáneos';

COMMENT ON COLUMN seguridad_ms.historial_sesiones.usuario_id IS 'Referencia al usuario que inició sesión';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.aplicacion_id IS 'Referencia a la aplicación a la que pertenece el usuario';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.rol_id IS 'Referencia al rol asignado al usuario';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.fecha_inicio IS 'Fecha y hora en que se inició la sesión';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.fecha_fin IS 'Fecha y hora en que se cerró la sesión';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.user_agent IS 'Cadena de texto que identifica el navegador y sistema operativo del usuario';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.token IS 'Identificador único del token (JTI) para permitir la revocación de sesiones específicas';
COMMENT ON COLUMN seguridad_ms.historial_sesiones.estado IS 'Estado de la sesión (ACTIVA, CERRADA, EXPIRADA, BLOQUEADA)';

-- Comentarios para MFA OTPs
COMMENT ON TABLE seguridad_ms.mfa_otps IS 'Almacén de corta duración de códigos temporalmente enviados por Correo o SMS';

COMMENT ON COLUMN seguridad_ms.mfa_otps.codigo IS 'Los 6 números que debe ingresar el enfermero';
COMMENT ON COLUMN seguridad_ms.mfa_otps.expira_en IS 'La hora exacta donde el código muere y ya no sirve';

-- Comentarios para gestión de contraseñas
COMMENT ON COLUMN seguridad_ms.usuarios.password_expirado_en IS 'Fecha en que la contraseña del usuario expira (indica cambio obligatorio cada 90 días)';

COMMENT ON TABLE seguridad_ms.password_history IS 'Historial de contraseñas anteriores para evitar reutilización';
COMMENT ON COLUMN seguridad_ms.password_history.usuario_id IS 'Usuario vinculado al historial de contraseñas';
COMMENT ON COLUMN seguridad_ms.password_history.password_hash IS 'Hash de la contraseña antigua, con bcrypt';
COMMENT ON COLUMN seguridad_ms.password_history.fecha_cambio IS 'Fecha y hora en que se registró la contraseña en el historial';

COMMENT ON TABLE seguridad_ms.password_reset_tokens IS 'Tokens temporales para recuperación de contraseña; únicos y expirables';
COMMENT ON COLUMN seguridad_ms.password_reset_tokens.usuario_id IS 'Usuario que solicita recuperación de contraseña';
COMMENT ON COLUMN seguridad_ms.password_reset_tokens.token IS 'Token de recuperación que se envía por email';
COMMENT ON COLUMN seguridad_ms.password_reset_tokens.fecha_creacion IS 'Timestamp en que se generó el token';
COMMENT ON COLUMN seguridad_ms.password_reset_tokens.fecha_expiracion IS 'Fecha/hora de vencimiento del token (15 minutos)';
COMMENT ON COLUMN seguridad_ms.password_reset_tokens.estirado IS 'Indicador de token usado (no reutilizable)';

-- Comentarios para índices de gestión de contraseñas
COMMENT ON INDEX idx_password_history_usuario_id IS 'Índice para buscar el historial de contraseñas por usuario';
COMMENT ON INDEX idx_password_history_fecha_cambio IS 'Índice para ordenar historial de contraseñas por fecha de cambio';
COMMENT ON INDEX idx_password_reset_token IS 'Índice para búsqueda rápida de token de recuperación';
COMMENT ON INDEX idx_password_reset_usuario_id IS 'Índice para listar tokens de recuperación por usuario';
COMMENT ON INDEX idx_password_reset_expiracion IS 'Índice para limpieza de tokens expirados y búsquedas por caducidad';

COMMENT ON TABLE seguridad_ms.refresh_tokens IS 'Refresh tokens emitidos para renovar access tokens sin solicitar nuevamente las credenciales del usuario';
COMMENT ON COLUMN seguridad_ms.refresh_tokens.usuario_id IS 'Identificador del usuario propietario del refresh token';
COMMENT ON COLUMN seguridad_ms.refresh_tokens.token IS 'Valor del refresh token emitido al cliente';
COMMENT ON COLUMN seguridad_ms.refresh_tokens.fecha_creacion IS 'Fecha y hora en que se emitio el refresh token';
COMMENT ON COLUMN seguridad_ms.refresh_tokens.fecha_expiracion IS 'Fecha y hora limite de validez del refresh token';
COMMENT ON COLUMN seguridad_ms.refresh_tokens.revocado IS 'Indica si el refresh token fue invalidado y ya no puede usarse para renovar access tokens';
COMMENT ON INDEX seguridad_ms.idx_refresh_tokens_usuario_id IS 'Índice para listar refresh tokens por usuario y permitir revocación masiva';