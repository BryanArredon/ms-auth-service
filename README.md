# 🛡️ Auth Service - Microservicio de Seguridad

Microservicio de autenticación y autorización construido con Spring Boot. Este servicio se encarga de gestionar aplicaciones, usuarios y roles del ecosistema de la Bitácora de Enfermería, generando tokens JWT para que otros microservicios puedan validar accesos de forma segura.

## 🚀 Tecnologías utilizadas

* **Java 17**
* **Spring Boot 3.x**
* **Spring Security & JWT** (Manejo de identidad)
* **Spring Data JPA** (Persistencia)
* **PostgreSQL** (Supabase)
* **Lombok** (Reducción de boilerplate)
* **OpenAPI / Swagger** (Documentación de API)

---

## ⚙️ Variables de Entorno (.env)

El proyecto requiere un archivo `.env` en la raíz (junto al `pom.xml`) para funcionar correctamente y no exponer credenciales. Crea tu `.env` guiándote del `.env.example`:

```env
DB_URL=jdbc:postgresql://<TU_HOST_SUPABASE>:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=<TU_PASSWORD>
```

---

## 💻 Instalación y Ejecución Local

1. Clona el repositorio y ubícate en la carpeta `auth-service`.
2. Asegúrate de tener **Java 17** instalado.
3. Configura tu archivo `.env` (ver sección anterior).
4. Compila y ejecuta la aplicación usando Maven Wrapper:

```bash
# Otorgar permisos de ejecución al wrapper (solo Mac/Linux)
chmod +x mvnw

# Limpiar y compilar
./mvnw clean compile

# Ejecutar el servidor de Spring Boot
./mvnw spring-boot:run
```

El servicio por defecto corre en el puerto **8080** (o el especificado en `application.properties`).

---

## 📚 Documentación de la API (Swagger)

Una vez que el proyecto esté corriendo, puedes explorar visualmente la API e interactuar con ella visitando:
* [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Endpoints Principales

* `POST /api/auth/register` - Registra un nuevo usuario en la Base de Datos con contraseña hasheada.
* `POST /api/auth/login` - Verifica credenciales y devuelve una respuesta de éxito (preparado para JWT).

---

## 📂 Estructura del Proyecto

```text
auth-service/
├── src/main/java/com/security/auth_service/
│   ├── config/       # Configuraciones de Seguridad, JWT y Swagger
│   ├── controller/   # Endpoints REST (ej. AuthController)
│   ├── dto/          # Data Transfer Objects (LoginRequest, AuthResponse)
│   ├── entity/       # Modelos JPA (Usuario, Rol, Aplicacion)
│   ├── repository/   # Interfaces de Spring Data
│   └── service/      # Lógica de negocio (AuthService, etc.)
└── src/main/resources/
    └── application.properties # Mapeo de DB al archivo .env
```
