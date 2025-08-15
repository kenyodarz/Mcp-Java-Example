package co.com.bancolombia.mcp.resources;

import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpResourceTemplatesConfig {

    @Bean
    public List<ResourceTemplate> resourceTemplates() {

        var userTemplate = new ResourceTemplate(
                "resource://users/{userId}",
                "User Info",
                "Plantilla para obtener información de un usuario dado su userId",
                "application/json",
                null
        );

        return List.of(userTemplate);
    }
}
