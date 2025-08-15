package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Configuration
public class McpResourcesConfig {

    @Bean
    public List<McpServerFeatures.AsyncResourceSpecification> asyncResources(ObjectMapper om) {

        var res = new McpSchema.Resource(
                "resource://system/info",
                "System information",
                "application/json",
                null, // mimeTypeHint
                null  // template
        );

        var spec = new McpServerFeatures.AsyncResourceSpecification(
                res,
                (McpAsyncServerExchange exchange, McpSchema.ReadResourceRequest req) -> Mono.fromCallable(() -> {
                    var info = Map.of(
                            "service", "mcp-bancolombia",
                            "status", "UP",
                            "reactive", true
                    );
                    String json = om.writeValueAsString(info);
                    return new McpSchema.ReadResourceResult(
                            List.of(new McpSchema.TextResourceContents(
                                    req.uri(),
                                    "application/json",
                                    json
                            ))
                    );
                })
        );

        return List.of(spec);
    }
}
