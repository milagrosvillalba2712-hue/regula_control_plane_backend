package com.regula.controlplane.config;

import com.regula.controlplane.service.ApiEventoControlPlaneService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApiEventoControlPlaneFilter extends OncePerRequestFilter {

    private final ApiEventoControlPlaneService apiEventoService;

    public ApiEventoControlPlaneFilter(ApiEventoControlPlaneService apiEventoService) {
        this.apiEventoService = apiEventoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.getAttribute(ApiEventoControlPlaneService.ATTR_SKIP_AUTO_EVENT) == null) {
                apiEventoService.registrarEntrada(request, response.getStatus(), (System.nanoTime() - start) / 1_000_000);
            }
        }
    }
}
