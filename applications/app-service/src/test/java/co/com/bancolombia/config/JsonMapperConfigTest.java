package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class JsonMapperConfigTest {

    @Test
    void testJsonMapperBean() {
        // ==================== GIVEN ====================
        // Preparar el contexto de aplicación con la configuración de JsonMapper
        AnnotationConfigApplicationContext applicationContextForTest = new AnnotationConfigApplicationContext(
                JsonMapperConfig.class);

        // ==================== WHEN ====================
        // Recuperar el bean de JsonMapper del contexto
        JsonMapper jsonMapperBean = applicationContextForTest.getBean(JsonMapper.class);

        // ==================== THEN ====================
        // Verificar que el bean fue creado correctamente
        assertNotNull(jsonMapperBean);
        assertTrue(true);

        // Limpiar recursos del contexto
        applicationContextForTest.close();
    }
}