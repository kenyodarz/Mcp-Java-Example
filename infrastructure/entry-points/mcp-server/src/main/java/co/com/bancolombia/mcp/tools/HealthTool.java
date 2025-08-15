package co.com.bancolombia.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class HealthTool {

    @Tool(name = "healthCheck", description = "Retorna 'OK' para health checks")
    public String healthCheck() {
        return "OK";
    }
}
