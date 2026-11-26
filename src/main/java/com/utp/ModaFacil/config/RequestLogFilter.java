package com.utp.ModaFacil.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        
        // Ignorar archivos estáticos para mantener limpia la consola
        if (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        long inicio = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long tiempoMs = (System.nanoTime() - inicio) / 1_000_000;
            log.info("[{}] {} -> Status: {} ({} ms)",
                    request.getMethod(),
                    uri,
                    response.getStatus(),
                    tiempoMs);
        }
    }
}
