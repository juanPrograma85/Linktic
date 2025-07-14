// src/main/java/linktic/producto/Infrastruture/security/ApiKeyAuthFilter.java
package linktic.inventario.Infrastructure.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String apiKey;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Paths that should be excluded from API key validation
    private final List<String> excludedPaths = Arrays.asList(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/health",
        "/health/**",
        "/h2-console/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Check if the request path should be excluded from API key validation
        boolean shouldExclude = excludedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        
        if (shouldExclude) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String requestApiKey = request.getHeader("X-API-KEY");

        if (!apiKey.equals(requestApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid API Key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

