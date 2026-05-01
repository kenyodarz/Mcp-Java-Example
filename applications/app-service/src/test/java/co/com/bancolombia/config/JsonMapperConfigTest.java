package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class JsonMapperConfigTest {

    @Test
    void testJsonMapperBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                JsonMapperConfig.class);
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
        assertNotNull(objectMapper);
        assertTrue(objectMapper instanceof ObjectMapperImp);
        context.close();
    }
}