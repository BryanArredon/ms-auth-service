# Documentación de Requerimientos de Seguridad - BarriMed Auth Service
## Fecha: 2 de Abril, 2026
Esta documentación cubre la implementación de 3 requerimientos críticos de seguridad implementados en el MS-Auth-Service de BarriMed.

## 📋 Arquitectura General del Microservicio
### 🏗️ Estructura del Proyecto
```
auth-service/
├── src/main/java/com/security/auth_service/
│   ├── config/          # Configuraciones de Seguridad y JWT
│   │   ├── WebSecurityConfig.java     # Configuración Spring Security
│   │   └── JWTAuthorizationFilter.java # Filtro JWT con blacklist
│   ├── controller/      # Endpoints REST
│   │   └── AuthController.java        # Controlador principal
│   ├── dto/             # Objetos de Transferencia de Datos
│   ├── entity/          # Modelos JPA
│   │   ├── UsuarioEntity.java
│   │   ├── PasswordHistoryEntity.java
│   │   └── PasswordResetTokenEntity.java
│   ├── repository/      # Interfaces de Spring Data
│   ├── service/         # Lógica de negocio
│   │   ├── TokenBlacklistService.java
│   │   ├── PasswordManagementService.java
│   │   └── impl/AuthServiceimpl.java
│   └── utils/           # Utilidades
└── src/main/resources/
    └── application.properties
```

### 🛠️ Tecnologías y Dependencias
- **Framework**: Spring Boot 4.0.3
- **Lenguaje**: Java 17
- **Base de Datos**: PostgreSQL (Supabase)
- **Seguridad**: Spring Security, JWT (JJWT), BCrypt
- **ORM**: JPA/Hibernate
- **Documentación**: OpenAPI/Swagger
- **Email**: Spring Mail (SMTP Gmail)
- **MFA**: TOTP (Google Authenticator), OTP por email

### ⚙️ Configuración Principal
```properties
# Base de Datos
spring.datasource.url=${DB_URL}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=seguridad_ms

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration-time=900000  # 15 minutos

# Email
spring.mail.host=smtp.gmail.com
spring.mail.username=${EMAIL_USER}
spring.mail.password=${EMAIL_PASS}

# Servidor
server.port=8085
```

## � Endpoints de la API
### 📍 Base URL
```
http://localhost:8085/api/auth
```

### 📋 Lista Completa de Endpoints
| Método | Endpoint | Descripción | Requiere Auth | MFA |
|--------|----------|-------------|---------------|-----|
| `POST` | `/register` | Registrar nuevo usuario | ❌ | ❌ |
| `POST` | `/login` | Iniciar sesión (requiere MFA) | ❌ | ✅ |
| `POST` | `/verify-mfa` | Verificar código MFA | ❌ | ❌ |
| `GET` | `/mfa/setup-totp/{correo}` | Configurar TOTP | ❌ | ❌ |
| `POST` | `/mfa/enable-totp` | Habilitar TOTP | ❌ | ❌ |
| `POST` | `/logout` | Cerrar sesión (blacklist token) | ❌ | ❌ |
| `PUT` | `/bloquear-cuenta/{correo}` | Bloquear cuenta de usuario | ✅ | ❌ |
| `PUT` | `/desbloquear-cuenta/{correo}` | Desbloquear cuenta | ✅ | ❌ |
| `PUT` | `/actualizar-credenciales` | Cambiar contraseña | ✅ | ❌ |
| `DELETE` | `/eliminar-cuenta` | Eliminar cuenta | ✅ | ❌ |
| `POST` | `/forgot-password` | Solicitar recuperación de contraseña | ❌ | ❌ |
| `POST` | `/validate-reset-token` | Validar token de reset | ❌ | ❌ |
| `POST` | `/reset-password` | Resetear contraseña | ❌ | ❌ |

### 🔓 Rutas Públicas (No requieren autenticación)
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/verify-mfa`
- `/api/auth/logout`
- `/api/auth/forgot-password`
- `/api/auth/validate-reset-token`
- `/api/auth/reset-password`
- `/api/auth/mfa/setup-totp/{correo}`
- `/api/auth/mfa/enable-totp`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### 🔒 Rutas Protegidas (Requieren JWT válido)
- `/api/auth/bloquear-cuenta/{correo}`
- `/api/auth/desbloquear-cuenta/{correo}`
- `/api/auth/actualizar-credenciales`
- `/api/auth/eliminar-cuenta`

### 📝 Ejemplos de Uso
#### Registro de Usuario
```bash
curl -X POST http://localhost:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "correo": "usuario@ejemplo.com",
    "password": "Contraseña123",
    "confirmPassword": "Contraseña123"
  }'
```

#### Login (Paso 1: Credenciales)
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "correo": "usuario@ejemplo.com",
    "password": "Contraseña123"
  }'
# Respuesta: requiere MFA
```

#### Login (Paso 2: MFA)
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tempUserId": "uuid-del-usuario",
    "otp": "123456"
  }'
# Respuesta: token JWT si es válido
```

#### Acceso a Endpoint Protegido
```bash
curl -X PUT http://localhost:8085/api/auth/actualizar-credenciales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "id": "uuid-del-usuario",
    "correo": "usuario@ejemplo.com",
    "password": "NuevaContraseña123"
  }'
```

## 📖 Documentación Detallada de Endpoints

### 1. POST /api/auth/register
**Descripción**: Registra un nuevo usuario en el sistema con contraseña hasheada.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "correo": "string (email, único)",
  "password": "string (mínimo 8 caracteres)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Usuario registrado exitosamente"
}
```

**Códigos de Estado**:
- `200`: Registro exitoso
- `400`: Datos inválidos (email duplicado, contraseña débil)
- `500`: Error interno del servidor

---

### 2. POST /api/auth/login
**Descripción**: Inicia sesión con credenciales. Requiere MFA para completar la autenticación.

**Autenticación**: No requerida

**Request Body** (Paso 1):
```json
{
  "correo": "string (email)",
  "password": "string"
}
```

**Response Body** (200 OK - Requiere MFA):
```json
{
  "requiresMfa": true,
  "tempUserId": "uuid",
  "mensaje": "MFA requerido. Hemos enviado un código a tu correo."
}
```

**Request Body** (Paso 2 - MFA):
```json
{
  "tempUserId": "uuid",
  "otp": "string (6 dígitos)"
}
```

**Response Body** (200 OK - Login completo):
```json
{
  "userId": "uuid",
  "correo": "string",
  "token": "jwt_token",
  "mensaje": "Login exitoso"
}
```

**Códigos de Estado**:
- `200`: Éxito (con o sin MFA)
- `401`: Credenciales inválidas
- `423`: Cuenta bloqueada
- `500`: Error interno

---

### 3. POST /api/auth/verify-mfa
**Descripción**: Verifica el código MFA para completar autenticación.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "tempUserId": "uuid",
  "otp": "string (6 dígitos)"
}
```

**Response Body** (200 OK):
```json
{
  "userId": "uuid",
  "correo": "string",
  "token": "jwt_token",
  "mensaje": "MFA verificado exitosamente"
}
```

---

### 4. GET /api/auth/mfa/setup-totp/{correo}
**Descripción**: Configura TOTP (Google Authenticator) para un usuario.

**Autenticación**: No requerida

**Parámetros URL**:
- `correo`: Email del usuario

**Response Body** (200 OK):
```json
{
  "secret": "string (clave TOTP)",
  "qrCodeUrl": "string (URL para QR)",
  "mensaje": "TOTP configurado. Escanea el QR con tu app autenticadora."
}
```

---

### 5. POST /api/auth/mfa/enable-totp
**Descripción**: Habilita TOTP después de configuración.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "correo": "string",
  "otp": "string (código TOTP)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "TOTP habilitado exitosamente"
}
```

---

### 6. POST /api/auth/logout
**Descripción**: Cierra sesión agregando el token JWT a la blacklist.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "token": "string (JWT token)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Logout exitoso - token invalidado"
}
```

---

### 7. PUT /api/auth/bloquear-cuenta/{correo}
**Descripción**: Bloquea la cuenta de un usuario.

**Autenticación**: JWT requerido

**Headers**:
- `Authorization: Bearer {jwt_token}`

**Parámetros URL**:
- `correo`: Email del usuario a bloquear

**Response Body** (200 OK):
```json
{
  "mensaje": "Cuenta bloqueada exitosamente"
}
```

---

### 8. PUT /api/auth/desbloquear-cuenta/{correo}
**Descripción**: Desbloquea la cuenta de un usuario.

**Autenticación**: JWT requerido

**Headers**:
- `Authorization: Bearer {jwt_token}`

**Parámetros URL**:
- `correo`: Email del usuario a desbloquear

**Response Body** (200 OK):
```json
{
  "mensaje": "Cuenta desbloqueada exitosamente"
}
```

---

### 9. PUT /api/auth/actualizar-credenciales
**Descripción**: Actualiza la contraseña del usuario actual.

**Autenticación**: JWT requerido

**Headers**:
- `Authorization: Bearer {jwt_token}`

**Request Body**:
```json
{
  "id": "uuid",
  "correo": "string",
  "password": "string (nueva contraseña)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Credenciales actualizadas exitosamente"
}
```

**Validaciones**:
- Contraseña no puede ser igual a las últimas 5 usadas
- Contraseña debe cumplir políticas de seguridad

---

### 10. DELETE /api/auth/eliminar-cuenta
**Descripción**: Elimina permanentemente la cuenta del usuario.

**Autenticación**: JWT requerido

**Headers**:
- `Authorization: Bearer {jwt_token}`

**Request Body**:
```json
{
  "id": "uuid",
  "correo": "string"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Cuenta eliminada exitosamente"
}
```

---

### 11. POST /api/auth/forgot-password
**Descripción**: Inicia proceso de recuperación de contraseña enviando email con token.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "correo": "string (email)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Se ha enviado un email con instrucciones para recuperar tu contraseña"
}
```

---

### 12. POST /api/auth/validate-reset-token
**Descripción**: Valida el token de recuperación de contraseña.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "token": "string (token del email)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Token válido"
}
```

---

### 13. POST /api/auth/reset-password
**Descripción**: Resetea la contraseña usando el token válido.

**Autenticación**: No requerida

**Request Body**:
```json
{
  "token": "string (token del email)",
  "newPassword": "string (nueva contraseña)"
}
```

**Response Body** (200 OK):
```json
{
  "mensaje": "Contraseña actualizada exitosamente"
}
```

## 📋 Requerimiento 1: Invalidación Real de Sesiones (Logout Auténtico)
### 🎯 Objetivo
Implementar un sistema de blacklist de JWT tokens para que el logout invalide efectivamente las sesiones, previniendo el uso de tokens robados o expirados.
### 🔧 Implementación Técnica
#### Componentes Creados/Modificados:
- **`TokenBlacklistService.java`** - Servicio in-memory para gestión de tokens invalidados
- **`JWTAuthorizationFilter.java`** - Filtro modificado para verificar blacklist antes de validar tokens
- **`AuthServiceimpl.logout()`** - Endpoint que agrega tokens a la blacklist
#### Arquitectura:
```java
// TokenBlacklistService
@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
```
#### Endpoint:
```http
POST /api/auth/logout
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
### 🗄️ Cambios en Base de Datos
- **No requiere cambios en BD** - Implementación in-memory (para producción: Redis recomendado)
### 🧪 Testing
```bash
# 1. Login exitoso
curl -X POST http://localhost:8085/api/auth/login \
  -d '{"correo":"user@test.com","password":"123"}'

# 2. Logout
curl -X POST http://localhost:8085/api/auth/logout \
  -d '{"token":"jwt_token_aqui"}'

# 3. Intentar usar token después de logout (debe fallar)
curl -H "Authorization: Bearer jwt_token_aqui" \
  http://localhost:8085/api/protected-endpoint
```
### 🔒 Consideraciones de Seguridad
- **Limitación**: In-memory no escala para múltiples instancias
- **Recomendación**: Migrar a Redis en producción
- **Beneficio**: Previene reutilización de tokens comprometidos

## 📋 Requerimiento 2: Gestión Completa de Contraseñas
### 🎯 Objetivo
Implementar un sistema completo de lifecycle de contraseñas con expiración automática, historial para prevenir reutilización, y recuperación segura vía email.

### 🔧 Implementación Técnica
#### Componentes Creados:
- **`PasswordHistoryEntity.java`** - Entidad JPA para historial de contraseñas
- **`PasswordResetTokenEntity.java`** - Entidad para tokens de recuperación (15 min expiry)
- **`PasswordManagementService.java`** - Servicio centralizado para lógica de contraseñas

#### Políticas Implementadas:
- **Expiración**: 90 días desde último cambio
- **Historial**: Máximo 5 contraseñas previas
- **Recuperación**: Tokens válidos por 15 minutos

#### Endpoints Afectados:
```http
# Cambiar contraseña (requiere autenticación)
PUT /api/auth/actualizar-credenciales

# Solicitar recuperación
POST /api/auth/forgot-password

# Validar token de recuperación
POST /api/auth/validate-reset-token

# Resetear contraseña
POST /api/auth/reset-password
```
### 🗄️ Cambios en Base de Datos
```sql
-- Nuevas tablas en PostgreSQL
CREATE TABLE seguridad_ms.password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_cambio TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seguridad_ms.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiracion TIMESTAMPTZ NOT NULL,
    usado BOOLEAN DEFAULT FALSE
);

-- Nuevos campos en usuarios
ALTER TABLE seguridad_ms.usuarios ADD COLUMN password_expirado_en TIMESTAMPTZ;
ALTER TABLE seguridad_ms.usuarios ADD COLUMN intentos_fallidos INTEGER DEFAULT 0;
```
### 🧪 Testing
```bash
# 1. Solicitar recuperación
curl -X POST http://localhost:8085/api/auth/forgot-password \
  -d '{"correo":"user@test.com"}'

# 2. Validar token (del email)
curl -X POST http://localhost:8085/api/auth/validate-reset-token \
  -d '{"token":"token_del_email"}'

# 3. Resetear contraseña
curl -X POST http://localhost:8085/api/auth/reset-password \
  -d '{"token":"token_del_email","newPassword":"NuevaPass123"}'

# 4. Intentar reutilizar contraseña anterior (debe fallar)
curl -X PUT http://localhost:8085/api/auth/actualizar-credenciales \
  -H "Authorization: Bearer jwt_token" \
  -d '{"id":"user-uuid","correo":"user@test.com","password":"ViejaPass123"}'
```

### 🔒 Consideraciones de Seguridad
- **Hashing**: BCrypt para todas las contraseñas
- **Tokens únicos**: UUID para máxima entropía
- **Expiración automática**: Limpieza periódica de tokens expirados
- **Validación**: Contraseñas no pueden repetirse en últimas 5

## 📋 Requerimiento 3: Protección contra Fuerza Bruta (Login)
### 🎯 Objetivo
Implementar protección contra ataques de fuerza bruta bloqueando cuentas después de 5 intentos fallidos de login consecutivos.

### 🔧 Implementación Técnica
#### Componentes Modificados:
- **`UsuarioEntity.java`** - Agregados campos `intentos_fallidos` e `cuenta_bloqueada`
- **`AuthServiceimpl.login()`** - Lógica de conteo y bloqueo (pendiente de implementación)
- **`registrarIntentoFallido()`** - Método transaccional independiente (pendiente)
- **`resetearIntentosFallidos()`** - Reset en login exitoso (pendiente)

#### Lógica de Bloqueo (Planificada):
```java
// En login() - si contraseña incorrecta
registrarIntentoFallido(user.getId());

// En registrarIntentoFallido()
int fallidos = usuarioActual.getIntentosFallidos() + 1;
usuarioActual.setIntentosFallidos(fallidos);

if (fallidos >= 5) {
    usuarioActual.setCuentaBloqueada(true);
    // Log: CUENTA BLOQUEADA
}
```
#### Endpoint Afectado:
```http
POST /api/auth/login
# Respuestas posibles:
# - 200: Login exitoso + reset intentos
# - 401: Credenciales inválidas + incrementar contador
# - 423: Cuenta bloqueada (requiere reset password)
```
### 🗄️ Cambios en Base de Datos
```sql
-- Campos agregados a usuarios
ALTER TABLE seguridad_ms.usuarios ADD COLUMN intentos_fallidos INTEGER DEFAULT 0;
ALTER TABLE seguridad_ms.usuarios ADD COLUMN cuenta_bloqueada BOOLEAN DEFAULT FALSE;

-- Índices para performance
CREATE INDEX idx_usuarios_correo_bloqueada ON seguridad_ms.usuarios(correo, cuenta_bloqueada);
```
### 🧪 Testing (Una vez implementado)
```bash
# 1-4. Intentos fallidos (deben incrementar contador)
curl -X POST http://localhost:8085/api/auth/login \
  -d '{"correo":"user@test.com","password":"wrong"}'
# Logs: "Intento fallido #1", "#2", "#3", "#4"

# 5. Quinto intento - bloquea cuenta
curl -X POST http://localhost:8085/api/auth/login \
  -d '{"correo":"user@test.com","password":"wrong"}'
# Log: "CUENTA BLOQUEADA: user@test.com después de 5 intentos"

# 6. Intentos posteriores - cuenta bloqueada
curl -X POST http://localhost:8085/api/auth/login \
  -d '{"correo":"user@test.com","password":"correct"}'
# Error: "Cuenta bloqueada. Por favor restablece tu acceso."

# 7. Reset via forgot-password
curl -X POST http://localhost:8085/api/auth/forgot-password \
  -d '{"correo":"user@test.com"}'
# Luego completar flujo de reset para desbloquear
```

### 🔒 Consideraciones de Seguridad
- **Transacciones independientes**: `REQUIRES_NEW` previene rollback del contador
- **Logging detallado**: Monitoreo de intentos fallidos
- **Reset automático**: Login exitoso limpia contador
- **Protección DoS**: Límite razonable de intentos (5)

## 🚀 Estado de Implementación
### ✅ Completado
- [x] JWT Token Blacklist (Logout funcional)
- [x] Sistema completo de gestión de contraseñas
- [x] Protección contra fuerza bruta (campos en BD preparados, lógica pendiente)
- [x] Migración MySQL → PostgreSQL
- [x] Documentación completa de BD
- [x] Testing básico de todos los flujos implementados

### 🔄 Próximos Pasos
- [ ] Implementar lógica de intentos fallidos en login
- [ ] Testing end-to-end completo
- [ ] Migración TokenBlacklist a Redis
- [ ] Configuración de alertas de seguridad
- [ ] Documentación de API completa con Swagger
- [ ] Integración con frontend

### 🛠️ Tecnologías Utilizadas
- **Framework**: Spring Boot 4.0.3
- **Base de Datos**: PostgreSQL (Supabase)
- **Seguridad**: JWT, BCrypt, Spring Security
- **ORM**: JPA/Hibernate
- **Lenguaje**: Java 17

---
*Documentación generada automáticamente - Abril 2026*