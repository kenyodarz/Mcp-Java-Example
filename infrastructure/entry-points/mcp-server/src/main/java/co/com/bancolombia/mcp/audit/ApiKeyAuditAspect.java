package co.com.bancolombia.mcp.audit;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Aspecto para auditar el uso de Tools, Resources y Prompts MCP
 * <p>
 * Este aspecto intercepta todas las llamadas a métodos anotados con
 *
 * @McpTool, @McpResource y @McpPrompt para registrar: - Quién (Client ID / User) - Qué
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

        // Información del metodo
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String argsString = formatArgs(args);

        // Ejecutar el metodo
        Object result = joinPoint.proceed();

        // Si es reactivo (Mono), inyectar lógica de auditoría en el flujo
        if (result instanceof Mono) {
            return ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .map(this::extractClientId)
                    .defaultIfEmpty("anonymous")
                    .flatMap(clientId -> {
                        log.info("📊 [AUDIT] {} llamado por: {} | Método: {}.{} | Args: {}",
                                mcpType,
                                clientId,
                                className,
                                methodName,
                                argsString);

                        return ((Mono<?>) result)
                                .doOnSuccess(value -> {
                                    long executionTime = System.currentTimeMillis() - startTime;
                                    log.info(
                                            "✅ [AUDIT] {} exitoso | Client: {} | Método: {}.{} | Tiempo: {}ms",
                                            mcpType,
                                            clientId,
                                            className,
                                            methodName,
                                            executionTime);
                                })
                                .doOnError(error -> {
                                    long executionTime = System.currentTimeMillis() - startTime;
                                    log.error(
                                            "❌ [AUDIT] {} fallido | Client: {} | Método: {}.{} | Tiempo: {}ms | Error: {}",
                                            mcpType,
                                            clientId,
                                            className,
                                            methodName,
                                            executionTime,
                                            error.getMessage());
                                });
                    });
        }

        // Para métodos síncronos (fallback básico, aunque SecurityContextHolder
        // probablemente esté vacío)
        // En una app Full Reactive esto raramente ocurrirá para endpoints WebFlux
        log.warn("⚠️ [AUDIT] Interceptado método no reactivo en aplicación WebFlux: {}.{}",
                className, methodName);
        return result;
    }

    private String extractClientId(Authentication auth) {
        if (auth == null) {
            return "anonymous";
        }

        if (auth instanceof JwtAuthenticationToken jwtauthenticationtoken) {
            Jwt jwt = jwtauthenticationtoken.getToken();

            // 1. Intentar 'appid' (Azure AD v1/Graph)
            String appid = jwt.getClaimAsString("appid");
            if (appid != null) {
                return appid;
            }

            // 2. Intentar 'azp' (Authorized Party - OIDC standard)
            String azp = jwt.getClaimAsString("azp");
            if (azp != null) {
                return azp;
            }

            // 3. Intentar extraer ClientID del 'aud'
            List<String> aud = jwt.getAudience();
            if (aud != null && !aud.isEmpty()) {
                // Heurística simple: devolver el primer audience
                return aud.getFirst();
            }
        }

        return auth.getName();
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