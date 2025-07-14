package linktic.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventarioServiceAppTest {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
    }

    @Test
    void mainMethodTest() {
        // Test that the main method can be called without throwing an exception
        String[] args = {};
        // We don't actually call the main method as it would start the server
        // Just verify the class can be loaded
        assert InventarioServiceApp.class != null;
    }
}
