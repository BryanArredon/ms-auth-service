# 🛡️ Hoja de Ruta (Roadmap): Mejoras al MS-Seguridad

- [ ] **1. Autenticación Multi-Factor Avanzada y Gratuita (MFA)**
  - [ ] **Cambio en BD:** Crear la tabla `usuario_mfa_metodos` (Relación 1 a N con usuarios).
  - [ ] **Email OTP (Obligatorio/Por Defecto):** Enviar un código de 6 dígitos al correo usando Spring Mail.
  - [ ] **App Authenticator (Respaldo 1):** Usar el algoritmo TOTP.
  - [ ] **Códigos de Recuperación (Respaldo 2):** Generar 10 códigos estáticos de un solo uso.

- [ ] **2. Protección contra Fuerza Bruta (Login)**
  - [ ] **Cambio en BD:** Agregar el campo `intentos_fallidos` (Numérico) a la tabla `usuarios`.
  - [ ] **Lógica:** Bloquear cuenta (`cuenta_bloqueada = TRUE`) tras 5 contraseñas incorrectas.

- [ ] **3. Estrategia de "Refresh Tokens"**
  - [ ] **Lógica:** Expedir un `Access Token` corto (15 min) y un `Refresh Token` largo (ej. 7 días).

- [ ] **4. Gestión Completa de Contraseñas**
  - [ ] **Recuperación:** Implementar endpoint de "Olvidé mi contraseña".
  - [ ] **Caducidad:** Campo `password_expirado` (caduca a los 90 días).
  - [ ] **Historial:** Cuidar que no repitan contraseñas previamente usadas.

- [ ] **5. Rate Limiting (Acelerador de Tráfico)**
  - [ ] **Lógica:** Implementar limitador (ej. `Bucket4j`) para bloquear por IP peticiones excesivas (max 10 por min).

- [ ] **6. Permisos Granulares (RBAC)**
  - [ ] **Cambio en BD:** Crear tabla de permisos y vincularlos a roles.
  - [ ] **Lógica:** Usar anotaciones como `@PreAuthorize("hasAuthority('EDITAR_BITACORA')")`.

- [ ] **7. Invalidación Real de Sesiones (Logout Auténtico)**
  - [ ] **Lógica:** Agregar JWT a una lista negra (memoria/Redis) al cerrar sesión e inspeccionarla en el `JWTAuthorizationFilter`.
