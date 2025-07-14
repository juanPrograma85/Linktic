// src/main/java/linktic/producto/Infrastruture/security/ApiKeyAuthFilter.java
package linktic.producto.Infrastruture.security;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String expectedApiKey;

    private static final String HEADER_NAME = "X-API-KEY";
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Define paths that should be excluded from API key authentication
    private final List<String> excludedPaths = Arrays.asList(
        "/health",
        "/health/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/h2-console/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludedPaths.stream().anyMatch(excludedPath -> pathMatcher.match(excludedPath, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader(HEADER_NAME);

        if (expectedApiKey.equals(requestApiKey)) {
            // Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken("api-user", null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid API Key\"}");
        }
    }
}
