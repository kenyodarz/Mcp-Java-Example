package co.com.bancolombia.mcp.config;

import co.com.bancolombia.mcp.prompts.BienvenidaPrompt;
import co.com.bancolombia.mcp.prompts.SaludoPrompt;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class McpPromptsConfig {

    private final SaludoPrompt saludoPrompt;
    private final BienvenidaPrompt bienvenidaPrompt;

    public McpPromptsConfig(SaludoPrompt saludoPrompt, BienvenidaPrompt bienvenidaPrompt) {
        this.saludoPrompt = saludoPrompt;
        this.bienvenidaPrompt = bienvenidaPrompt;
        log.info("McpPromptsConfig initialized with SaludoPrompt: {} and BienvenidaPrompt: {}",
                saludoPrompt, bienvenidaPrompt);
    }

    @Bean
    public List<McpStatelessServerFeatures.AsyncPromptSpecification> prompts() {
        log.info("Creating prompts bean...");
        return List.of(
                saludoPrompt.getPromptSpecification(),
                bienvenidaPrompt.getPromptSpecification()
        );
    }
}