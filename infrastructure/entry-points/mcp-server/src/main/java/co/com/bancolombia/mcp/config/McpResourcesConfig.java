package co.com.bancolombia.mcp.config;

import co.com.bancolombia.mcp.resources.SystemInfoResource;
import co.com.bancolombia.mcp.resources.UserInfoResource;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceSpecification;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class McpResourcesConfig {

    private final SystemInfoResource systemInfoResource;
    private final UserInfoResource userInfoResource;

    public McpResourcesConfig(SystemInfoResource systemInfoResource,
            UserInfoResource userInfoResource) {
        this.systemInfoResource = systemInfoResource;
        this.userInfoResource = userInfoResource;
        log.info(
                "McpResourcesConfig initialized with SystemInfoResource: {} and UserInfoResource: {}",
                systemInfoResource, userInfoResource);
    }

    @Bean
    public List<AsyncResourceSpecification> asyncResources() {
        log.info("Creating asyncResources bean...");
        return List.of(
                systemInfoResource.getResourceSpecification(),
                userInfoResource.getResourceSpecification()
        );
    }
}