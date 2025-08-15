package co.com.bancolombia.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
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

    @Bean
    public WebFluxSseServerTransportProvider sseServerTransport() {
        return WebFluxSseServerTransportProvider.builder()
                .sseEndpoint("/sse")
                .messageEndpoint("/mcp/messages")
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransportProvider transport) {
        return transport.getRouterFunction();
    }

    @Bean
    public McpAsyncServer mcpAsyncServer() {

        var capabilities = ServerCapabilities.builder()
                .resources(false, true) // Allow resources to be registered
                .tools(true) // Allow tools to be registered
                .prompts(true) // Allow prompts to be registered
                .logging() // Enable logging
                .completions() // Enable completions
                .build();

        return McpServer.async(sseServerTransport())
                .serverInfo("mcp-bancolombia", "1.0.0")
                .capabilities(capabilities)
                .resourceTemplates(templates)
                .resources(resources)
                .prompts(prompts)
                .tools(tools)
                .build();
    }
}
