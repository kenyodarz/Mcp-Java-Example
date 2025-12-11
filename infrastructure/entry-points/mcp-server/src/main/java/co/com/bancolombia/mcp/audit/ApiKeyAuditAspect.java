package co.com.bancolombia.mcp.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Aspecto para auditar el uso de Tools, Resources y Prompts MCP
 * <p>
 * Este aspecto intercepta todas las llamadas a métodos anotados con
 *
 * @McpTool, @McpResource y @McpPrompt para registrar: - Quién (API Key ID) - Qué
 * (metodo/tool/resource) - Cuándo (timestamp) - Resultado (éxito/fallo) - Tiempo de ejecución
 */
@Slf4j
@Aspect
@Component
public class ApiKeyAuditAspect {

    /**
     * Audita todas las llamadas a Tools MCP
     */
    @Around("@annotation(org.springaicommunity.mcp.annotation.McpTool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditMcpCall(joinPoint, "TOOL");
    }

    /**
     * Audita todas las llamadas a Resources MCP
     */
    @Around("@annotation(org.springaicommunity.mcp.annotation.McpResource)")
    public Object auditResourceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditMcpCall(joinPoint, "RESOURCE");
    }

    /**
     * Audita todas las llamadas a Prompts MCP
     */
    @Around("@annotation(org.springaicommunity.mcp.annotation.McpPrompt)")
    public Object auditPromptCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditMcpCall(joinPoint, "PROMPT");
    }

    /**
     * Metodo genérico de auditoría
     */
    private Object auditMcpCall(ProceedingJoinPoint joinPoint, String mcpType) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Obtener información de autenticación
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String apiKeyId = auth != null ? auth.getName() : "anonymous";

        // Información del metodo
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("📊 [AUDIT] {} llamado por API Key: {} | Método: {}.{} | Args: {}",
                mcpType,
                apiKeyId,
                className,
                methodName,
                formatArgs(args));

        try {
            // Ejecutar el metodo
            Object result = joinPoint.proceed();

            // Si es reactivo (Mono), agregar auditoría al flujo
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                        .doOnSuccess(value -> {
                            long executionTime = System.currentTimeMillis() - startTime;
                            log.info(
                                    "✅ [AUDIT] {} exitoso | API Key: {} | Método: {}.{} | Tiempo: {}ms",
                                    mcpType,
                                    apiKeyId,
                                    className,
                                    methodName,
                                    executionTime);
                        })
                        .doOnError(error -> {
                            long executionTime = System.currentTimeMillis() - startTime;
                            log.error(
                                    "❌ [AUDIT] {} fallido | API Key: {} | Método: {}.{} | Tiempo: {}ms | Error: {}",
                                    mcpType,
                                    apiKeyId,
                                    className,
                                    methodName,
                                    executionTime,
                                    error.getMessage());
                        });
            }

            // Para métodos síncronos
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("✅ [AUDIT] {} exitoso | API Key: {} | Método: {}.{} | Tiempo: {}ms",
                    mcpType,
                    apiKeyId,
                    className,
                    methodName,
                    executionTime);

            return result;

        } catch (Throwable error) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error(
                    "❌ [AUDIT] {} fallido | API Key: {} | Método: {}.{} | Tiempo: {}ms | Error: {}",
                    mcpType,
                    apiKeyId,
                    className,
                    methodName,
                    executionTime,
                    error.getMessage());
            throw error;
        }
    }

    /**
     * Formatea los argumentos para el log (limita el tamaño)
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(args.length, 3); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String argStr = arg.toString();
                // Limitar tamaño del argumento en el log
                sb.append(argStr.length() > 50 ? argStr.substring(0, 50) + "..." : argStr);
            }
        }

        if (args.length > 3) {
            sb.append(", ... (").append(args.length - 3).append(" more)");
        }

        sb.append("]");
        return sb.toString();
    }
}