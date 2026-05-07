# Estado de Implementación - Manejo de Errores y Fallback

## 📋 Resumen Ejecutivo

Se ha implementado un sistema **completo de resilencia** para la API externa (Simpsons API) con
fallback automático, retry con backoff exponencial, y circuit breaker. La documentación de
troubleshooting ha sido actualizada para guiar al usuario en caso de errores.

---

## ✅ IMPLEMENTADO

### 1. **Resilience4j Integration**

**Archivo**:
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/RestConsumer.java`

#### Características:

- ✅ **Circuit Breaker** (`simpsonsApi`): Protege el sistema de caídas en cascada
- ✅ **Retry**: Reintenta automáticamente 3 veces con backoff exponencial
- ✅ **TimeLimiter**: Timeout de 5 segundos por request

#### Código:

```java

@CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getCharacterByIdFallback")
@Retry(name = "simpsonsApi")
@TimeLimiter(name = "simpsonsApi")
public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
    return client.get()
            .uri("/characters/{id}", id)
            .retrieve()
            .bodyToMono(SimpsonsCharacterResponse.class)
            .doOnError(error -> log.warning("Error: " + error.getMessage()));
}
```

### 2. **Fallback Methods**

Implementados 3 métodos de fallback (uno por cada tipo de dato):

```java
// Retorna datos por defecto cuando fallan todos los reintentos
public Mono<SimpsonsCharacterResponse> getCharacterByIdFallback(int id, Exception ex) {
    return Mono.just(SimpsonsCharacterResponse.builder()
            .id(-1)
            .name("Personaje desconocido")
            .status("FALLBACK_DATA")
            .description("No se pudo obtener información de la API. " + ex.getMessage())
            .age(0)
            .build());
}
```

**Características del fallback**:

- ID = -1 para identificar datos por defecto
- Status = "FALLBACK_DATA" para marcar que no son datos reales
- Descripción incluye el error original para debugging

### 3. **Configuración en application.yaml**

Ya está configurado en: `applications/app-service/src/main/resources/application.yaml`

```yaml
resilience4j:
  circuitbreaker:
    instances:
      simpsonsApi:
        registerHealthIndicator: true
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2s
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowSize: 10
        minimumNumberOfCalls: 10
        waitDurationInOpenState: 10s
        automaticTransitionFromOpenToHalfOpenEnabled: true

  retry:
    instances:
      simpsonsApi:
        maxAttempts: 3
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2

  timelimiter:
    instances:
      simpsonsApi:
        timeoutDuration: 5s
```

### 4. **Error Logging**

Se agregó logging usando `@Log` de Lombok:

```java

@Log
public class RestConsumer {
    log.info("Fetching character with id: "+id);
    log.warning("Error fetching character "+id +": "+error.getMessage());
}
```

### 5. **Documentación Actualizada**

El archivo `docs/troubleshooting.md` ahora incluye:

- ✅ Guía para "Connection timeout to Simpsons API"
- ✅ Guía para "Simpsons API return 404 Not Found"
- ✅ Guía para "Simpsons API return 500 Server Error"
- ✅ Guía para "Circuit Breaker OPEN"
- ✅ Guía para "Retry exceeded, returning fallback"

---

## ⚠️ POR IMPLEMENTAR

### 1. **Cache para Fallback Inteligente**

**Prioridad**: ALTA  
**Razón**: Actualmente el fallback retorna datos por defecto, pero sería mejor guardar en caché los
últimos datos exitosos

**Implementación sugerida**:

```java
// Usar Spring Cache o Caffeine
@Cacheable(value = "characters", key = "#id")
@CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getCharacterFromCacheFallback")
public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
    // ...
}

public Mono<SimpsonsCharacterResponse> getCharacterFromCacheFallback(int id, Exception ex) {
    // Retornar desde caché si existe
}
```

**Archivos a crear/modificar**:

-
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/config/CacheConfig.java` (
nuevo)
-
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/RestConsumer.java` (
modificar)
- `applications/app-service/src/main/resources/application.yaml` (agregar configuración de caché)

**Dependencia requerida** (ya debería estar en gradle):

```gradle
dependencies {
implementation '
org.springframework.boot:spring-boot-starter-cache'
implementation 'com.github.ben-manes.caffeine:caffeine'
}
```

### 2. **Rate Limiting**

**Prioridad**: MEDIA  
**Razón**: Proteger la API externa de rate limiting

**Implementación sugerida**:

```java

@RateLimiter(name = "simpsonsApi")
@CircuitBreaker(name = "simpsonsApi", fallbackMethod = "...")
public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
    // ...
}
```

**Configuración en application.yaml**:

```yaml
resilience4j:
  ratelimiter:
    instances:
      simpsonsApi:
        registerHealthIndicator: true
        limitRefreshPeriod: 10s
        limitForPeriod: 100  # 100 requests cada 10 segundos
        timeoutDuration: 1s
```

### 3. **Bulkhead (Isolation)**

**Prioridad**: MEDIA  
**Razón**: Aislar threads por servicio para evitar bloqueo de toda la aplicación

**Implementación sugerida**:

```java

@Bulkhead(name = "simpsonsApi")
@CircuitBreaker(name = "simpsonsApi", fallbackMethod = "...")
public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
    // ...
}
```

**Configuración** (en application.yaml):

```yaml
resilience4j:
  bulkhead:
    instances:
      simpsonsApi:
        maxConcurrentCalls: 10  # Máximo 10 llamadas concurrentes
        maxWaitDuration: 2s
```

### 4. **Métricas Personalizadas**

**Prioridad**: BAJA  
**Razón**: Monitoreo detallado del comportamiento del API

**Archivos a crear**:

-
`infrastructure/entry-points/mcp-server/src/main/java/co/com/bancolombia/mcp/metrics/SimpsonsApiMetrics.java` (
nuevo)

**Ejemplo**:

```java

@Component
public class SimpsonsApiMetrics {

    private final MeterRegistry meterRegistry;

    public void recordFallback(String resourceType) {
        meterRegistry.counter("simpsons_api.fallback", "type", resourceType).increment();
    }

    public void recordRetry(String resourceType, int attempts) {
        meterRegistry.timer("simpsons_api.retry", "type", resourceType)
                .record(Duration.ofSeconds(1));
    }
}
```

### 5. **Circuit Breaker Health Indicator Extendido**

**Prioridad**: BAJA  
**Razón**: Dashboard mejorado en `/actuator/health`

**Archivos a crear**:

-
`infrastructure/entry-points/mcp-server/src/main/java/co/com/bancolombia/mcp/health/SimpsonsApiHealthIndicator.java` (
nuevo)

### 6. **Tests para Escenarios de Fallo**

**Prioridad**: ALTA  
**Razón**: Verificar que el fallback funciona correctamente

**Archivos a crear**:

-
`infrastructure/driven-adapters/rest-consumer/src/test/java/co/com/bancolombia/consumer/SimpsonsApiResilience4jTest.java` (
nuevo)

**Test sugerido**:

```java

@Test
void shouldReturnFallbackWhenCircuitBreakerIsOpen() {
    // Simular múltiples fallos
    // Verificar que circuit breaker se abre
    // Verificar que fallback retorna datos correctos
}

@Test
void shouldRetryThreeTimesBeforeFallback() {
    // Simular timeout
    // Verificar que se reintenta 3 veces
    // Verificar backoff exponencial
}
```

### 7. **Documentación en Código**

**Prioridad**: MEDIA  
**Razón**: Documentación inline para otros desarrolladores

**Agregar JavaDoc**:

```java
/**
 * Obtiene un personaje por su ID con resilencia automática.
 *
 * Comportamiento:
 * - Reintenta hasta 3 veces con backoff exponencial
 * - Si falla, abre circuit breaker si hay 50% de fallos
 * - Con circuit breaker abierto, retorna fallback inmediatamente
 * - Timeout: 5 segundos por request
 *
 * @param id ID del personaje (1-100 aprox)
 * @return Personaje o datos por defecto si falla
 */
public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
```

---

## 📊 Matriz de Tareas

| Tarea                   | Estado      | Prioridad | Estimación | Dependencias    |
|-------------------------|-------------|-----------|------------|-----------------|
| Circuit Breaker         | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| Retry                   | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| TimeLimiter             | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| Fallback Methods        | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| Error Logging           | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| Documentación           | ✅ HECHO     | ALTA      | ✓          | Ninguna         |
| **Cache para Fallback** | ⏳ PENDIENTE | ALTA      | 2-3h       | RC Implementado |
| **Rate Limiting**       | ⏳ PENDIENTE | MEDIA     | 1-2h       | RC Implementado |
| **Bulkhead**            | ⏳ PENDIENTE | MEDIA     | 1-2h       | RC Implementado |
| **Métricas Custom**     | ⏳ PENDIENTE | BAJA      | 2-3h       | RC Implementado |
| **Health Indicator**    | ⏳ PENDIENTE | BAJA      | 1-2h       | RC Implementado |
| **Tests de Fallo**      | ⏳ PENDIENTE | ALTA      | 3-4h       | RC Implementado |
| **JavaDoc**             | ⏳ PENDIENTE | MEDIA     | 1h         | Todas           |

---

## 🔧 Cómo Usar la Resiliencia Implementada

### Verificar el Estado del Circuit Breaker

```bash
curl http://localhost:8080/actuator/health
# Ver en "components.simpsonsApicircuitbreaker.state"
```

### Simular Fallo y Ver Fallback

```java
// Cambiar URL por una inválida en application.yaml
adapter.restconsumer.url=http://invalid-url.com

// Ejecutar request
// Resultado: fallback con id=-1 y status="FALLBACK_DATA"
```

### Ver Logs de Error

```bash
grep "Circuit breaker\|Retry exceeded\|fallback" logs/application.log
```

---

## 📝 Notas Importantes

1. **El fallback es automático**: No requiere código adicional en los use cases o tools
2. **El status="FALLBACK_DATA"** permite identificar datos sintéticos en la aplicación cliente
3. **Los datos de fallback tienen id=-1** para evitar conflictos con IDs reales
4. **La configuración es totalmente personalizable** en `application.yaml`
5. **El circuit breaker se abre con 50% de tasa de fallos** en una ventana de 10 llamadas

---

## 🚀 Próximos Pasos Recomendados

1. **Implementar Cache** (si la API no cambia frecuentemente)
2. **Agregar Rate Limiting** (si hay límites en la API externa)
3. **Escribir tests** para validar los escenarios de fallo
4. **Monitorear métricas** en `/actuator/metrics`
5. **Documentar SLA** (Service Level Agreement) basado en el comportamiento de fallback

---

## 📚 Referencias

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot + Resilience4j Integration](https://spring.io/blog/2021/04/29/resilience4j-reactive-functional-integration)
- [Troubleshooting Guide](./troubleshooting.md#-problemas-con-api-externa-simpsons-api)
- [Architecture](./architecture.md)


