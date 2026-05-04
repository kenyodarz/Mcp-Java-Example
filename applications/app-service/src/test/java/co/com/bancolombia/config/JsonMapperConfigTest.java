package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class JsonMapperConfigTest {

    @Test
    void testJsonMapperBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                JsonMapperConfig.class);
        JsonMapper objectMapper = context.getBean(JsonMapper.class);
        assertNotNull(objectMapper);
        assertTrue(true);
        context.close();
    }
}