# Auth Service - Microservicio de Seguridad

Microservicio de autenticación y autorización construido con Spring Boot.
Este servicio se encarga de gestionar usuarios, autenticación y generación de tokens JWT para que otros microservicios puedan validar accesos.

## Tecnologías utilizadas

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* JWT
* Lombok
* OpenAPI / Swagger

---

# Estructura del proyecto

```
auth-service
│
├── src/main/java/com/security/auth_service
│
│   ├── config
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── security
│   ├── exception
│   └── AuthServiceApplication.java
│
├── src/main/resources
│   └── application.properties
│
└── pom.xml
```

---

# Descripción de cada carpeta

## config

Contiene las configuraciones globales del sistema.

Se utiliza para:

* Configuración de seguridad
* Configuración de Swagger/OpenAPI
* Configuración de CORS
* Beans globales

Ejemplo de clases:

```
SecurityConfig.java
OpenApiConfig.java
```

---

## controller

Contiene los controladores REST del sistema.

Se encargan de recibir las peticiones HTTP y devolver respuestas al cliente.

Ejemplos de endpoints:

```
POST /auth/login
POST /auth/register
POST /auth/refresh-token
GET  /auth/validate
```

Ejemplo de clase:

```
AuthController.java
```

---

## service

Contiene la lógica de negocio del microservicio.

Aquí se implementan las reglas del sistema como:

* autenticación de usuarios
* generación de tokens
* registro de usuarios
* validación de credenciales

Ejemplo de clases:

```
AuthService.java
UserService.java
```

---

## repository

Capa encargada de interactuar con la base de datos.

Utiliza Spring Data JPA para realizar operaciones CRUD.

Ejemplo de repositorios:

```
UserRepository.java
RoleRepository.java
```

---

## entity

Contiene las entidades que representan las tablas de la base de datos.

Ejemplo:

```
User.java
Role.java
```

Ejemplo de relación:

```
User -> ManyToMany -> Role
```

---

## dto

Los DTO (Data Transfer Objects) se utilizan para transportar datos entre el cliente y el servidor.

Sirven para evitar exponer directamente las entidades de la base de datos.

Ejemplo de DTOs:

```
LoginRequest.java
RegisterRequest.java
AuthResponse.java
```

---

## security

Contiene toda la lógica relacionada con la seguridad del sistema.

Se utiliza para:

* generación de tokens JWT
* validación de tokens
* filtros de autenticación
* manejo de autenticación en Spring Security

Ejemplo de clases:

```
JwtService.java
JwtAuthenticationFilter.java
JwtUtils.java
```

---

## exception

Manejo centralizado de errores del sistema.

Permite controlar y formatear las respuestas cuando ocurre una excepción.

Ejemplo de clases:

```
GlobalExceptionHandler.java
CustomException.java
```

---

# Flujo de autenticación

```
Cliente
   │
   ▼
Auth Service (Login)
   │
   ▼
Generación de JWT
   │
   ▼
Cliente usa el token en otros microservicios
```

---

# Ejemplo de endpoints

```
POST /auth/login
POST /auth/register
POST /auth/refresh-token
GET  /auth/validate
```

---

# Objetivo del microservicio

Este microservicio permite centralizar la autenticación del sistema para que otros microservicios puedan utilizar el mismo mecanismo de seguridad basado en JWT.
