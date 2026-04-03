# Rate Limiting Implementation - Documentación

## 📋 Descripción General

Se ha implementado un sistema de **Rate Limiting** para proteger el servicio de autenticación contra ataques DDoS y ataques de fuerza bruta. Utiliza la biblioteca **Bucket4j** de manera eficiente.

## 🎯 Características

### ✅ Implementado
- **Protección de endpoints**: Login, Forgot Password, Verify MFA
- **Límite por IP**: 10 intentos por minuto por dirección IP
- **Identificación de IP**: Soporta proxies (X-Forwarded-For, X-Real-IP)
- **Error Estándar HTTP**: Retorna 429 Too Many Requests
- **Configurable**: Fácil de ajustar los parámetros

### 📊 Bucket4j - ¿Cómo Funciona?

```
┌─────────────────────────────────────┐
│    IP: 192.168.1.100                │
├─────────────────────────────────────┤
│ 🪣 BUCKET (10 tokens al inicio)     │
│                                     │
│ Intento 1: ✓ (9 tokens quedan)      │
│ Intento 2: ✓ (8 tokens quedan)      │
│ ...                                 │
│ Intento 10: ✓ (0 tokens quedan)     │
│ Intento 11: ✗ 429 Too Many Requests│
│                                     │
│ Después de 1 minuto: Se refill      │
│ Con 10 tokens nuevamente            │
└─────────────────────────────────────┘
```

## ⚙️ Configuración

### En `application.properties`:

```properties
# Rate Limiting Configuration
app.rate-limit.enabled=true           # Habilitar/Deshabilitar
app.rate-limit.max-requests=10        # Máximo de solicitudes permitidas
app.rate-limit.window-minutes=1       # Ventana de tiempo en minutos
```

### Ejemplos de Configuración

**Más Restrictivo (desarrollo/testing):**
```properties
app.rate-limit.max-requests=3
app.rate-limit.window-minutes=1
```

**Más Permisivo (producción):**
```properties
app.rate-limit.max-requests=50
app.rate-limit.window-minutes=1
```

**Ventana más larga:**
```properties
app.rate-limit.max-requests=20
app.rate-limit.window-minutes=5
```

## 🔍 Endpoints Protegidos

- `POST /api/auth/login` - Protegido ✅
- `POST /api/auth/forgot-password` - Protegido ✅
- `POST /api/auth/verify-mfa` - Protegido ✅

## 📡 Respuesta de Error (429)

Cuando se excede el límite:

```json
{
  "error": "Too Many Requests",
  "message": "Ha excedido el límite de intentos permitidos. Intente más tarde.",
  "status": 429,
  "timestamp": 1712138400000
}
```

## 🧪 Pruebas Manual

### Con cURL (simulando múltiples intentos):

```bash
# Prueba 1-10: Deberían funcionar (200-201 o autenticación fallida)
for i in {1..10}; do
  echo "Intento $i"
  curl -X POST http://localhost:8085/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"correo":"test@example.com","contrasena":"password"}'
  sleep 0.5
done

# Prueba 11: Debería retornar 429
echo "Intento 11 (Should be 429)"
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test@example.com","contrasena":"password"}' \
  -w "\nStatus: %{http_code}\n"
```

### Con Postman:

1. Crear una colección "Auth Test"
2. Crear un request POST a `http://localhost:8085/api/auth/login`
3. Agregar body JSON:
   ```json
   {
     "correo": "test@example.com",
     "contrasena": "password"
   }
   ```
4. Enviar el request 11 veces rápidamente
5. El intento 11 debería retornar 429

### Con Python:

```python
import requests
import time

url = "http://localhost:8085/api/auth/login"
payload = {
    "correo": "test@example.com",
    "contrasena": "password"
}

# Hacer 15 intentos
for i in range(1, 16):
    response = requests.post(url, json=payload)
    print(f"Intento {i}: Status {response.status_code}")
    
    if response.status_code == 429:
        print(f"✗ Rate limit alcanzado en intento {i}")
        print(f"Response: {response.json()}")
        break
    
    time.sleep(0.1)
```

## 📁 Archivos Creados/Modificados

### Archivos Nuevos:
1. **RateLimitService.java** - Servicio que gestiona los buckets
2. **RateLimitInterceptor.java** - Interceptor que aplica el rate limiting
3. **RateLimitProperties.java** - Propiedades configurables
4. **WebConfig.java** - Configuración para registrar interceptores

### Archivos Modificados:
1. **pom.xml** - Agregada dependencia de Bucket4j
2. **application.properties** - Agregadas propiedades de configuración

## 🛡️ Ventajas de esta Implementación

✅ **En memoria**: Sin dependencia de base de datos externa
✅ **Eficiente**: Usa HashMap concurrente para almacenar buckets
✅ **Escalable**: Funciona bien en un solo servidor o con balanceador de carga
✅ **Configurable**: Todos los parámetros están en properties
✅ **Estándar**: Usa código HTTP 429 estándar
✅ **Flexible**: Fácil de agregar más endpoints o cambiar límites

## 🔧 Mantenimiento

### Limpiar cache (opcional):
Si necesitas limpiar el cache de buckets programáticamente:

```java
@Autowired
private RateLimitService rateLimitService;

// En algún controlador o servicio
rateLimitService.clearCache();
```

### Monitoreo:
Puedes extender el RateLimitService para registrar logs:

```java
if (!rateLimitService.allowRequest(ip)) {
    logger.warn("Rate limit exceeded for IP: {}", ip);
    // enviar alerta
}
```

## 🚀 Próximos Pasos (Opcional)

- [ ] Agregar persistencia en Redis para clusters distribuidos
- [ ] Agregar métricas de Prometheus
- [ ] Crear dashboard de monitoreo
- [ ] Implementar whitelist de IPs confiables
- [ ] Agregar rate limiting por usuario (en lugar de solo por IP)

## ⚠️ Notas Importantes

- **En memoria**: Los buckets se pierden si la aplicación se reinicia
- **Una instancia**: Esta implementación es para un único servidor. Para múltiples servidores, considerar Redis
- **IP Real**: En producción, asegúrate que el servidor recibe la IP real del cliente (configuración del proxy/load balancer)

## 📚 Referencias

- [Bucket4j Documentación](https://github.com/vladimir-bukhtoyarov/bucket4j)
- [HTTP Status Codes - 429](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429)
- [OWASP Rate Limiting](https://owasp.org/www-community/attacks/Brute_force_attack)
