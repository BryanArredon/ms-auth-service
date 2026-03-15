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
    password_hash TEXT NOT NULL,
    cuenta_bloqueada BOOLEAN DEFAULT FALSE,
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
COMMENT ON COLUMN seguridad_ms.usuarios.cuenta_bloqueada IS 'Indica si la cuenta del usuario está bloqueada por seguridad o administración';
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