package linktic.inventario.Infrastructure.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthFilterTest {

    @InjectMocks
    private ApiKeyAuthFilter apiKeyAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private final String VALID_API_KEY = "inventarioMicroKey2025$";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        
        ReflectionTestUtils.setField(apiKeyAuthFilter, "apiKey", VALID_API_KEY);
    }

    @Test
    void testDoFilterInternal_ValidApiKey_AllowsRequest() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        request.addHeader("X-API-KEY", VALID_API_KEY);

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDoFilterInternal_InvalidApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        request.addHeader("X-API-KEY", "invalid-key");

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized - Invalid API Key"));
    }

    @Test
    void testDoFilterInternal_MissingApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        // No API key header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized - Invalid API Key"));
    }

    @Test
    void testDoFilterInternal_SwaggerEndpoint_AllowsRequest() throws ServletException, IOException {
        // Given
        request.setRequestURI("/swagger-ui/index.html");
        // No API key header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDoFilterInternal_SwaggerApiDocsEndpoint_AllowsRequest() throws ServletException, IOException {
        // Given
        request.setRequestURI("/v3/api-docs");
        // No API key header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDoFilterInternal_HealthEndpoint_AllowsRequest() throws ServletException, IOException {
        // Given
        request.setRequestURI("/health");
        // No API key header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDoFilterInternal_SwaggerResourcesEndpoint_AllowsRequest() throws ServletException, IOException {
        // Given
        request.setRequestURI("/swagger-resources/configuration/ui");
        // No API key header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDoFilterInternal_EmptyApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        request.addHeader("X-API-KEY", "");

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized - Invalid API Key"));
    }

    @Test
    void testDoFilterInternal_NullApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        // Don't add any header (null header)

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized - Invalid API Key"));
    }

    @Test
    void testDoFilterInternal_CaseInsensitiveHeader_ValidatesCorrectly() throws ServletException, IOException {
        // Given
        request.setRequestURI("/inventarios/1");
        request.addHeader("x-api-key", VALID_API_KEY); // lowercase header

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testIsPublicEndpoint_SwaggerPaths() {
        // Test various swagger paths
        assertTrue(isPublicEndpoint("/swagger-ui.html"));
        assertTrue(isPublicEndpoint("/swagger-ui/"));
        assertTrue(isPublicEndpoint("/swagger-ui/index.html"));
        assertTrue(isPublicEndpoint("/v3/api-docs"));
        assertTrue(isPublicEndpoint("/v3/api-docs/swagger-config"));
        assertTrue(isPublicEndpoint("/swagger-resources"));
        assertTrue(isPublicEndpoint("/swagger-resources/configuration/ui"));
    }

    @Test
    void testIsPublicEndpoint_HealthPath() {
        assertTrue(isPublicEndpoint("/health"));
    }

    @Test
    void testIsPublicEndpoint_PrivatePaths() {
        // The ApiKeyAuthFilter requires API key for ALL requests except those in excludedPaths
        // /api/productos/1 is NOT in excludedPaths, so it requires API key (protected)
        assertFalse(isPublicEndpoint("/api/productos/1"));
        // /admin is also NOT in excludedPaths, so it also requires API key (protected)
        assertFalse(isPublicEndpoint("/admin"));
        // /inventarios/* is also NOT in excludedPaths, so it requires API key too
        assertFalse(isPublicEndpoint("/inventarios/1"));
    }

    // Helper method to test if a path is public/excluded from authentication
    private boolean isPublicEndpoint(String path) {
        try {
            MockHttpServletRequest testRequest = new MockHttpServletRequest();
            MockHttpServletResponse testResponse = new MockHttpServletResponse();
            MockFilterChain testFilterChain = new MockFilterChain();
            
            testRequest.setRequestURI(path);
            
            // Don't set any API key header to test if the path is excluded
            
            apiKeyAuthFilter.doFilterInternal(testRequest, testResponse, testFilterChain);
            
            // If status is not 401 (unauthorized), then the path is public/excluded
            return testResponse.getStatus() != HttpServletResponse.SC_UNAUTHORIZED;
        } catch (Exception e) {
            return false;
        }
    }
}
