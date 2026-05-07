# Troubleshooting

Guía para resolver problemas comunes al usar el Code Review MCP Server.

---

## 🔧 Problemas de Conexión

### Error: "Connection refused"

**Síntoma**: El cliente MCP no puede conectarse al servidor.

**Causas posibles**:

1. El servidor no está ejecutándose
2. Puerto incorrecto
3. Firewall bloqueando la conexión

**Solución**:

```bash
# 1. Verificar que el servidor esté corriendo
curl http://localhost:8080/actuator/health

# 2. Si no responde, iniciar el servidor
gradle :applications:app-service:bootRun

# 3. Verificar el puerto en application.yaml
# server.port: 8080
```

---

## 🔐 Problemas de Autenticación

### Error: "401 Unauthorized"

**Síntoma**: Todas las requests retornan 401.

**Causas posibles**:

1. API Key inválida o mal formateada
2. API Key deshabilitada
3. API Key expirada
4. Header faltante

**Solución**:

```bash
# 1. Verificar formato correcto: {id}.{secret}
# Correcto: dev-client.dev-secret-key-12345
# Incorrecto: dev-client

# 2. Verificar en H2 Console
# http://localhost:8080/h2-console
SELECT id, enabled, expires_at 
FROM api_keys 
WHERE id = 'dev-client';

# 3. Verificar que el header esté presente
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{"method": "tools/list"}'
```

### Error: "Header X-API-Key not found"

**Síntoma**: Error indicando que falta el header de autenticación.

**Solución**: Asegúrate de incluir el header en todas las requests:

```bash
-H "X-API-Key: dev-client.dev-secret-key-12345"
```

---

## 📡 Problemas con Tools

### Error: "Repository not found"

**Síntoma**: La herramienta `analyze_repository` falla indicando que no encuentra el repositorio.

**Causas posibles**:

1. Nombre del repositorio incorrecto
2. Repositorio no existe en Azure DevOps
3. Sin permisos de lectura sobre el repositorio

**Solución**:

1. Verificar el nombre exacto en Azure DevOps
2. Confirmar permisos de lectura
3. Verificar que el proyecto sea el correcto:

```bash
# Proyecto configurado:
# b267af7c-3233-4ad1-97b3-91083943100d
```

### Error: "Timeout waiting for analysis"

**Síntoma**: `check_status` devuelve `RUNNING` por más de 1 hora.

**Causas posibles**:

1. Repositorio muy grande
2. Muchas dependencias
3. AWS Step Function bloqueada

**Solución**:

1. Ten paciencia, el timeout máximo es de 24 horas
2. Analiza subdirectorios específicos usando el parámetro `path`:

```json
{
  "repository_name": "My-Java-Project",
  "path": "/specific-module"
}
```

3. Verifica el estado en AWS Console:
    - Step Functions > Executions
    - Buscar por `execution_id`

### Error: "Tool not found"

**Síntoma**: Error indicando que el tool no existe.

**Causas posibles**:

1. Nombre del tool incorrecto
2. Tool no está registrado

**Solución**:

```bash
# Listar tools disponibles
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{"method": "tools/list"}'
```

---

## 🔗 Problemas con API Externa (Simpsons API)

### Error: "Connection timeout to Simpsons API"

**Síntoma**: Requests al servicio de Simpsons demoran más de 5 segundos.

**Causas posibles**:

1. API externa está lenta
2. Red inestable o latencia alta
3. El timeout configurado es muy corto

**Solución**:

1. Verifica el timeout en `application.yaml`:

```yaml
adapter:
   restconsumer:
      timeout: 5000  # Milisegundos
```

2. Aumentar si es necesario:

```yaml
adapter:
   restconsumer:
      timeout: 10000  # 10 segundos
```

3. El sistema automáticamente:
   - **Reintenta 3 veces** con backoff exponencial
   - **Usa fallback** con datos por defecto si falla
   - Retorna datos en caché si están disponibles

### Error: "Simpsons API return 404 Not Found"

**Síntoma**: Error indicando que el personaje/episodio/ubicación no existe.

**Causas posibles**:

1. ID incorrecto
2. Elemento fue eliminado de la API
3. El servidor está en mantenimiento

**Solución**:

```bash
# Validar que el ID sea correcto
# IDs válidos de personajes: 1-100 (aprox)

curl "https://thesimpsonsapi.com/api/characters/1"
# Respuesta exitosa: {"id": 1, "name": "Homer Simpson", ...}

curl "https://thesimpsonsapi.com/api/characters/99999"
# Respuesta: 404 Not Found
```

**Fallback**: Si el API retorna 404, el sistema:

1. **Reintenta una vez más** (por si es error temporal)
2. **Retorna un objeto por defecto** con valores seguros
3. **Registra el error** en los logs

### Error: "Simpsons API return 500 Server Error"

**Síntoma**: La API externa está retornando errores de servidor.

**Causas posibles**:

1. API externa está caída
2. Mantenimiento programado
3. Error interno del servidor

**Solución**:

1. Verificar el estado de la API:

```bash
curl -I "https://thesimpsonsapi.com/api/characters/1"
# HTTP/1.1 200 OK ✅ (está operativa)
# HTTP/1.1 500 Internal Server Error ❌ (tiene problemas)
```

2. El sistema automáticamente:
   - **Reintenta 3 veces** con espera exponencial
   - **Abre el circuit breaker** después de múltiples fallos
   - **Retorna fallback** mientras se recupera

3. Espera 10 segundos antes de reintentar (ver logs):

```bash
grep "Circuit breaker" logs/application.log
# Si ves "OPEN", espera a que se cierre
```

### Error: "Circuit Breaker OPEN"

**Síntoma**: Todos los requests fallan inmediatamente, error: `CircuitBreakerOpenException`.

**Causas posibles**:

1. Múltiples fallos consecutivos a la API
2. El circuito se abrió para proteger el sistema
3. Está en estado "HALF_OPEN" esperando recuperación

**Cómo funciona el Circuit Breaker**:

```
CLOSED (normal)
    ↓ (50% fallos)
OPEN (se bloquea)
    ↓ (10s después)
HALF_OPEN (prueba)
    ↓
CLOSED o OPEN
```

**Solución**:

1. Esperar a que se recupere (máximo 10 segundos):

```bash
# Ver estado actual
curl http://localhost:8080/actuator/health
# Buscar en "components": circuitbreakers

# Salida esperada:
#{
#  "components": {
#    "simpsonsApicircuitbreaker": {
#      "status": "UP",
#      "details": {
#        "state": "CLOSED"  # ✅ Ok
#      }
#    }
#  }
#}
```

2. Si sigue abierto, revisar logs:

```bash
grep "simpsonsApi" logs/application.log
# Buscar últimos errores
```

3. Reiniciar el servidor si persiste:

```bash
# Ctrl+C para detener
gradle :applications:app-service:bootRun
```

### Error: "Retry exceeded, returning fallback"

**Síntoma**: Después de 3 intentos, el sistema retorna datos por defecto.

**¿Por qué ocurre?**:

1. API externa no responde
2. Network error o timeout
3. Todos los reintentos fallaron

**Ejemplo de respuesta fallback**:

```json
{
   "id": -1,
   "name": "Personaje desconocido",
   "status": "FALLBACK_DATA",
   "description": "No se pudo obtener información de la API",
   "age": 0
}
```

**Solución**:

1. Verificar lógica de negocio que depende de estos datos
2. El fallback tiene marcador `status: "FALLBACK_DATA"`
3. Manejar gracefully en la aplicación cliente

---

## 🔄 Problemas Reactivos

### Error: "Sync providers doesn't support reactive return types"

**Síntoma**: Error al iniciar el servidor indicando incompatibilidad con tipos reactivos.

**Causa**: Métodos MCP no retornan `Mono<T>` en servidor ASYNC.

**Solución**: Cambiar el tipo de retorno a `Mono<T>`:

```java
// ❌ Incorrecto (para servidores ASYNC)
@McpTool(name = "health")
public String health() {
    return "healthy";
}

// ✅ Correcto
@McpTool(name = "health")
public Mono<String> health() {
    return Mono.just("healthy");
}
```

### Error: "Cannot resolve method 'text()'"

**Síntoma**: Error de compilación en Resources.

**Causa**: No se está haciendo casting a `TextResourceContents`.

**Solución**:

```java
// ❌ Incorrecto
String text = result.contents().get(0).text();

// ✅ Correcto
ResourceContents content = result.contents().get(0);
TextResourceContents textContent = (TextResourceContents) content;
String text = textContent.text();
```

---

## 🗄️ Problemas con H2 Console

### Error: "H2 Console no carga"

**Síntoma**: `http://localhost:8080/h2-console` no responde.

**Solución**:

1. Verificar que esté habilitada en `application.yaml`:

```yaml
spring:
  h2:
    console:
      enabled: true
```

2. Reiniciar el servidor
3. Acceder a: `http://localhost:8080/h2-console`
4. Usar credenciales:
    - **JDBC URL**: `jdbc:h2:mem:mcpdb`
    - **User**: `sa`
    - **Password**: (vacío)

### Error: "Wrong user name or password"

**Síntoma**: No puede conectarse a H2 Console.

**Solución**: Verificar credenciales:

- **JDBC URL**: `jdbc:h2:mem:mcpdb` (exacto)
- **User**: `sa`
- **Password**: dejar vacío

---

## 🚀 Problemas de Compilación

### Error: "Template not found"

**Síntoma**: Error al generar entry point MCP.

**Solución**:

```bash
# Limpiar cache de Gradle
gradle --stop
Remove-Item -Recurse -Force .gradle\configuration-cache
gradle clean build
```

### Error: "Build path errors"

**Síntoma**: Errores de compilación relacionados con el plugin.

**Solución**: Compilar y publicar el plugin localmente:

```bash
cd scaffold-clean-architecture
gradle clean build publishToMavenLocal
```

---

## 🖥️ Problemas con Claude Desktop

### Claude Desktop no detecta el servidor

**Síntoma**: El servidor no aparece en Claude Desktop.

**Solución**:

1. Verificar que el JAR se compiló:

```bash
gradle :applications:app-service:bootJar
ls applications/app-service/build/libs/
```

2. Verificar configuración en `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "bancolombia-code-review": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sse-client",
        "http://localhost:8080/mcp/stream"
      ],
      "env": {
        "X-API-Key": "claude-desktop.claude-secret-key-abcde"
      }
    }
  }
}
```

3. Verificar logs de Claude Desktop:
    - **Windows**: `%APPDATA%\Claude\logs\`
    - **macOS**: `~/Library/Logs/Claude/`

4. Probar el servidor manualmente:

```bash
java -jar applications/app-service/build/libs/app-service.jar
```

---

## 📊 Problemas de Performance

### Servidor lento o no responde

**Síntoma**: Requests tardan mucho tiempo.

**Causas posibles**:

1. Muchas requests concurrentes
2. Timeout muy largo
3. Memoria insuficiente

**Solución**:

1. Ajustar timeout en `application.yaml`:

```yaml
spring:
  ai:
    mcp:
      server:
        request-timeout: "10s"  # Reducir de 30s
```

2. Aumentar memoria JVM:

```bash
java -Xmx2G -jar app-service.jar
```

3. Verificar métricas:

```bash
curl http://localhost:8080/actuator/metrics
```

---

## 🔍 Debugging

### Habilitar Logs Detallados

```yaml
logging:
  level:
    io.modelcontextprotocol: DEBUG
    org.springframework.ai.mcp: DEBUG
    co.com.bancolombia: DEBUG
    org.springframework.web: DEBUG
```

### Ver Logs en Tiempo Real

```bash
# Windows
Get-Content -Path logs\application.log -Wait -Tail 50

# Linux/Mac
tail -f logs/application.log
```

### Buscar Errores Específicos

```bash
# Buscar errores de autenticación
grep "401\|Unauthorized" logs/application.log

# Buscar timeouts
grep "timeout\|TimeoutException" logs/application.log

# Buscar errores de Azure DevOps
grep "Azure\|DevOps" logs/application.log
```

---

## ❓ FAQ

### ¿Cómo sé si el servidor está funcionando?

```bash
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### ¿Cómo reseteo la base de datos H2?

La base de datos H2 es en memoria, se resetea al reiniciar el servidor:

```bash
# Detener servidor (Ctrl+C)
# Iniciar servidor
gradle :applications:app-service:bootRun
```

### ¿Cómo cambio el puerto del servidor?

Edita `application.yaml`:

```yaml
server:
  port: 9090  # Cambiar de 8080 a 9090
```

### ¿Cómo agrego un nuevo Tool?

1. Crear clase en `infrastructure/entry-points/mcp-server/tools/`
2. Anotar con `@Component`
3. Crear método con `@McpTool`
4. Retornar `Mono<T>`

```java
@Component
public class MyNewTool {
    
    @McpTool(name = "my_tool", description = "Mi nuevo tool")
    public Mono<String> execute() {
        return Mono.just("Result");
    }
}
```

---

## 📞 Soporte

Si el problema persiste:

1. **Revisar logs**: `logs/application.log`
2. **Consultar H2 Console**: `http://localhost:8080/h2-console`
3. **Verificar Azure DevOps
   **: [Pipeline](https://dev.azure.com/grupobancolombia/b267af7c-3233-4ad1-97b3-91083943100d/_build?definitionId=55727)
4. **Contactar equipo**: Prácticas de Ingeniería de Software

---

**💡 Tip**: La mayoría de los problemas se resuelven verificando los logs y la configuración de API
Keys. Siempre revisa primero `logs/application.log` y H2 Console.

Para más información:

- [Getting Started](getting-started.md)
- [Architecture](architecture.md)
- [Security](security.md)
