# Guía de Implementación - Cache para Fallback Inteligente

## 🎯 Objetivo

Implementar un sistema de caché que guarde los últimos datos exitosos de la API externa, para que
cuando se active el fallback, se retornen datos reales (aunque posiblemente desactualizados) en
lugar de datos sintéticos.

---

## 📋 Pasos de Implementación

### Paso 1: Verificar Dependencia de Spring Cache

**Archivo**: `infrastructure/driven-adapters/rest-consumer/build.gradle`

Verificar que esté presente:

```gradle
dependencies {
    // Debería estar en spring-boot-starter-webflux, pero verificar:
implementation '
org.springframework.boot:spring-boot-starter-cache'
implementation 'com.github.ben-manes.caffeine:caffeine'
}
```

### Paso 2: Crear Configuración de Cache

**Crear**:
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/config/CacheConfig.java`

```java
package co.com.bancolombia.consumer.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "simpsonsCharacters",
                "simpsonsEpisodes",
                "simpsonsLocations"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)  // Caducar después de 1 hora
                .maximumSize(1000)  // Máximo 1000 entradas
                .recordStats()  // Registrar estadísticas
        );
        return cacheManager;
    }
}
```

### Paso 3: Actualizar RestConsumer

**Modificar**:
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/RestConsumer.java`

```java
package co.com.bancolombia.consumer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log
public class RestConsumer {

    private static final String FALLBACK_ERROR_SUFFIX = ". Using fallback data. Error: ";
    private static final String API_INFO_ERROR_MESSAGE = "No se pudo obtener información de la API. ";

    private final WebClient client;
    private final CacheStore cacheStore;  // Nuevo: servicio para acceder al caché

    // ============================================
    // MÉTODOS CON CACHÉ
    // ============================================

    @Cacheable(value = "simpsonsCharacters", key = "#id")
    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getCharacterByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
        log.info("Fetching character with id: " + id);
        return client.get()
                .uri("/characters/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsCharacterResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching character " + id + ": " + error.getMessage()));
    }

    @Cacheable(value = "simpsonsEpisodes", key = "#id")
    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getEpisodeByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsEpisodeResponse> getEpisodeById(int id) {
        log.info("Fetching episode with id: " + id);
        return client.get()
                .uri("/episodes/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsEpisodeResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching episode " + id + ": " + error.getMessage()));
    }

    @Cacheable(value = "simpsonsLocations", key = "#id")
    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getLocationByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsLocationResponse> getLocationById(int id) {
        log.info("Fetching location with id: " + id);
        return client.get()
                .uri("/locations/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsLocationResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching location " + id + ": " + error.getMessage()));
    }

    // ============================================
    // MÉTODOS DE FALLBACK CON CACHÉ
    // ============================================

    @SuppressWarnings("unused")
    public Mono<SimpsonsCharacterResponse> getCharacterByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for character " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());

        // Intentar recuperar del caché
        Optional<SimpsonsCharacterResponse> cached = cacheStore.getCharacterFromCache(id);
        if (cached.isPresent()) {
            log.info("Returning cached character data for id: " + id);
            return Mono.just(cached.get());
        }

        // Si no hay caché, retornar datos por defecto
        return Mono.just(SimpsonsCharacterResponse.builder()
                .id(-1)
                .name("Personaje desconocido")
                .status("FALLBACK_DATA")
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .age(0)
                .build());
    }

    @SuppressWarnings("unused")
    public Mono<SimpsonsEpisodeResponse> getEpisodeByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for episode " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());

        Optional<SimpsonsEpisodeResponse> cached = cacheStore.getEpisodeFromCache(id);
        if (cached.isPresent()) {
            log.info("Returning cached episode data for id: " + id);
            return Mono.just(cached.get());
        }

        return Mono.just(SimpsonsEpisodeResponse.builder()
                .id(-1)
                .name("Episodio desconocido")
                .season(0)
                .episodeNumber(0)
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .build());
    }

    @SuppressWarnings("unused")
    public Mono<SimpsonsLocationResponse> getLocationByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for location " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());

        Optional<SimpsonsLocationResponse> cached = cacheStore.getLocationFromCache(id);
        if (cached.isPresent()) {
            log.info("Returning cached location data for id: " + id);
            return Mono.just(cached.get());
        }

        return Mono.just(SimpsonsLocationResponse.builder()
                .id(-1)
                .name("Ubicación desconocida")
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .build());
    }

    // ============================================
    // MÉTODOS DE GESTIÓN DEL CACHÉ
    // ============================================

    @CacheEvict(value = "simpsonsCharacters", key = "#id")
    public void invalidateCharacterCache(int id) {
        log.info("Invalidating cache for character " + id);
    }

    @CacheEvict(value = "simpsonsEpisodes", key = "#id")
    public void invalidateEpisodeCache(int id) {
        log.info("Invalidating cache for episode " + id);
    }

    @CacheEvict(value = "simpsonsLocations", key = "#id")
    public void invalidateLocationCache(int id) {
        log.info("Invalidating cache for location " + id);
    }

    @CacheEvict(value = {"simpsonsCharacters", "simpsonsEpisodes",
            "simpsonsLocations"}, allEntries = true)
    public void clearAllCaches() {
        log.info("Clearing all Simpsons API caches");
    }
}
```

### Paso 4: Crear Servicio de Acceso al Caché

**Crear**:
`infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/consumer/CacheStore.java`

```java
package co.com.bancolombia.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log
public class CacheStore {

    private final CacheManager cacheManager;

    public Optional<SimpsonsCharacterResponse> getCharacterFromCache(int id) {
        var cache = cacheManager.getCache("simpsonsCharacters");
        if (cache == null)
            return Optional.empty();

        var value = cache.get(id);
        if (value == null)
            return Optional.empty();

        log.info("Retrieved character " + id + " from cache");
        return Optional.of((SimpsonsCharacterResponse) value.get());
    }

    public Optional<SimpsonsEpisodeResponse> getEpisodeFromCache(int id) {
        var cache = cacheManager.getCache("simpsonsEpisodes");
        if (cache == null)
            return Optional.empty();

        var value = cache.get(id);
        if (value == null)
            return Optional.empty();

        log.info("Retrieved episode " + id + " from cache");
        return Optional.of((SimpsonsEpisodeResponse) value.get());
    }

    public Optional<SimpsonsLocationResponse> getLocationFromCache(int id) {
        var cache = cacheManager.getCache("simpsonsLocations");
        if (cache == null)
            return Optional.empty();

        var value = cache.get(id);
        if (value == null)
            return Optional.empty();

        log.info("Retrieved location " + id + " from cache");
        return Optional.of((SimpsonsLocationResponse) value.get());
    }

    public void printCacheStats() {
        var charCache = cacheManager.getCache("simpsonsCharacters");
        if (charCache != null
                && charCache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
            var nativeCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) charCache.getNativeCache();
            log.info("Cache Stats: " + nativeCache.stats());
        }
    }
}
```

### Paso 5: Actualizar application.yaml

**Archivo**: `applications/app-service/src/main/resources/application.yaml`

Agregar después de la sección de `adapter.restconsumer`:

```yaml
# ============================================
# CONFIGURACIÓN DE CACHÉ
# ============================================
spring:
  cache:
    type: caffeine
    caffeine:
      spec: "expireAfterWrite=1h,maximumSize=1000"
    cache-names:
      - simpsonsCharacters
      - simpsonsEpisodes
      - simpsonsLocations

# Logging para Caffeine
logging:
  level:
    com.github.benmanes.caffeine: WARN
```

### Paso 6: Crear Tests

**Crear**:
`infrastructure/driven-adapters/rest-consumer/src/test/java/co/com/bancolombia/consumer/CacheIntegrationTest.java`

```java
package co.com.bancolombia.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CacheIntegrationTest {

    @Autowired
    private RestConsumer restConsumer;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheCharacterData() {
        // Primer request - desde API
        restConsumer.getCharacterById(1)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Verificar que está en caché
        var cache = cacheManager.getCache("simpsonsCharacters");
        assertThat(cache.get(1)).isNotNull();

        // Segundo request - desde caché (más rápido)
        restConsumer.getCharacterById(1)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldReturnCachedDataOnFallback() {
        // Simular request exitoso para llenar caché
        restConsumer.getCharacterById(1)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Ahora simular fallo (requiere mock del WebClient)
        // El fallback debería retornar datos del caché
    }

    @Test
    void shouldInvalidateCacheWhenRequested() {
        // Llenar caché
        restConsumer.getCharacterById(1)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Invalidar
        restConsumer.invalidateCharacterCache(1);

        // Verificar que fue removido
        var cache = cacheManager.getCache("simpsonsCharacters");
        assertThat(cache.get(1)).isNull();
    }

    @Test
    void shouldClearAllCaches() {
        // Llenar múltiples cachés
        restConsumer.getCharacterById(1)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Limpiar todos
        restConsumer.clearAllCaches();

        // Verificar que están vacíos
        assertThat(cacheManager.getCache("simpsonsCharacters").get(1)).isNull();
    }
}
```

---

## 🔍 Cómo Verificar el Funcionamiento del Caché

### 1. Ver Estadísticas del Caché

Agregar endpoint en un controller (OPCIONAL):

```java

@RestController
@RequestMapping("/cache")
public class CacheStatsController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                stats.put(name, "Size: " +
                        (cache.getNativeCache() instanceof java.util.Map ?
                                ((java.util.Map) cache.getNativeCache()).size() : "N/A"));
            }
        });

        return stats;
    }

    @PostMapping("/clear")
    public String clearCache() {
        cacheManager.getCacheNames().forEach(name ->
                cacheManager.getCache(name).clear()
        );
        return "Cache cleared";
    }
}
```

### 2. Ver Logs de Caché

```bash
grep "Retrieved\|Invalidating" logs/application.log
```

### 3. Simular Fallo y Ver Fallback

```java
// 1. Hacer request exitoso (se guarda en caché)
curl "http://localhost:8080/simpsons/characters/1"

// 2. Apagar API externa o simular fallo
// 3. Hacer request nuevamente
// Resultado: Verás datos en caché en lugar de fallback sintético
```

---

## 📊 Comportamiento del Caché

```
Request 1 (sin caché)
    ↓
Llama API externa
    ↓
Guarda en caché
    ↓
Retorna datos

Request 2 (mismo ID)
    ↓
Recupera del caché
    ↓
Retorna datos (sin llamar API)

Request 3 (API falla)
    ↓
Circuit Breaker ABIERTO
    ↓
Fallback: Busca en caché
    ↓
Caché HIT: Retorna datos reales
    ↓
Caché MISS: Retorna fallback sintético
```

---

## ⚙️ Configuración Recomendada

```yaml
# Para desarrollo (datos frescos)
spring.cache.caffeine.spec: "expireAfterWrite=10m,maximumSize=100"

# Para producción (más datos en caché)
spring.cache.caffeine.spec: "expireAfterWrite=1h,maximumSize=5000,recordStats"

# Alta concurrencia
spring.cache.caffeine.spec: "expireAfterWrite=30m,maximumSize=10000,recordStats,weakKeys"
```

---

## 🚀 Próximos Pasos

1. Ejecutar tests: `./gradlew :rest-consumer:test`
2. Compilar: `./gradlew build`
3. Probar con el servidor corriendo
4. Monitorear logs para validar fallback

---

## 📚 Referencias

- [Spring Cache Documentation](https://spring.io/guides/gs/caching/)
- [Caffeine Documentation](https://github.com/ben-manes/caffeine/wiki)
- [Resilience4j + Caching Pattern](https://resilience4j.readme.io/)


