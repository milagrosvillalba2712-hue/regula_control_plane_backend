package com.regula.controlplane.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final String adminKey;
    private final String clientKey;

    public ApiKeyFilter(@Value("${regula.control-plane.admin-api-key}") String adminKey,
                        @Value("${regula.control-plane.client-api-key}") String clientKey) {
        this.adminKey = adminKey;
        this.clientKey = clientKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator/health")) {
            chain.doFilter(request, response);
            return;
        }
        String apiKey = request.getHeader("X-API-Key");
        boolean adminPath = path.startsWith("/api/admin/");
        boolean allowed = adminPath ? adminKey.equals(apiKey) : clientKey.equals(apiKey) || adminKey.equals(apiKey);
        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API_KEY_INVALIDA\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}

