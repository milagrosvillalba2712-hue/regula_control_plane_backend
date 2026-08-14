package com.regula.controlplane.config;

import com.regula.controlplane.service.ApiEventoControlPlaneService;
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
    private final String adminSessionToken;
    private final ApiEventoControlPlaneService apiEventoService;

    public ApiKeyFilter(@Value("${regula.control-plane.admin-api-key}") String adminKey,
                        @Value("${regula.control-plane.client-api-key}") String clientKey,
                        @Value("${regula.control-plane.admin-session-token}") String adminSessionToken,
                        ApiEventoControlPlaneService apiEventoService) {
        this.adminKey = adminKey;
        this.clientKey = clientKey;
        this.adminSessionToken = adminSessionToken;
        this.apiEventoService = apiEventoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/actuator/health")
                || path.equals("/api/admin/login")) {
            chain.doFilter(request, response);
            return;
        }
        String apiKey = request.getHeader("X-API-Key");
        String authorization = request.getHeader("Authorization");
        boolean bearerAdmin = authorization != null
                && authorization.startsWith("Bearer ")
                && adminSessionToken.equals(authorization.substring("Bearer ".length()));
        boolean adminPath = path.startsWith("/api/admin/");
        boolean allowed = adminPath
                ? adminKey.equals(apiKey) || bearerAdmin
                : clientKey.equals(apiKey) || adminKey.equals(apiKey) || bearerAdmin;
        if (!allowed) {
            auditInvalidApiKey(request);
            request.setAttribute(ApiEventoControlPlaneService.ATTR_SKIP_AUTO_EVENT, true);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API_KEY_INVALIDA\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private void auditInvalidApiKey(HttpServletRequest request) {
        apiEventoService.registrarErrorSeguridad(request, "API_KEY_INVALIDA",
                "Intento de acceso con API key invalida o token no autorizado");
    }
}
