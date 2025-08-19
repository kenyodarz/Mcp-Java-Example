package co.com.bancolombia.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.WebFluxStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

@Configuration
@Slf4j
public class McpServerConfig {

    private final List<McpServerFeatures.AsyncToolSpecification> tools;
    private final List<McpServerFeatures.AsyncResourceSpecification> resources;
    private final List<McpServerFeatures.AsyncPromptSpecification> prompts;
    private final List<McpSchema.ResourceTemplate> templates;
    private final ObjectMapper objectMapper;

    public McpServerConfig(
            List<McpServerFeatures.AsyncToolSpecification> tools,
            List<McpServerFeatures.AsyncResourceSpecification> resources,
            List<McpServerFeatures.AsyncPromptSpecification> prompts,
            List<McpSchema.ResourceTemplate> templates,
            ObjectMapper objectMapper
    ) {
        this.tools = tools;
        this.resources = resources;
        this.prompts = prompts;
        this.templates = templates;
        this.objectMapper = objectMapper;
    }

    /**
     * Configuración del transporte HTTP Streamable.
     * Usa application/json-stream y respuestas chunked.
     */
    @Bean
    public WebFluxStreamableServerTransportProvider streamableServerTransport() {
        return WebFluxStreamableServerTransportProvider.builder()
                .messageEndpoint("/mcp/stream")
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public RouterFunction<?> mcpRouterFunction(WebFluxStreamableServerTransportProvider transport) {
        return transport.getRouterFunction();
    }

    @Bean
    public McpAsyncServer mcpAsyncServer(WebFluxStreamableServerTransportProvider transport) {

        var capabilities = ServerCapabilities.builder()
                .resources(false, true) // habilita recursos
                .tools(true)            // habilita tools
                .prompts(true)          // habilita prompts
                .logging()              // habilita logging
                .completions()          // habilita completions
                .build();

        return McpServer.async(transport)
                .serverInfo("mcp-bancolombia", "1.0.0")
                .capabilities(capabilities)
                .resourceTemplates(templates)
                .resources(resources)
                .prompts(prompts)
                .tools(tools)
                .build();
    }
}
