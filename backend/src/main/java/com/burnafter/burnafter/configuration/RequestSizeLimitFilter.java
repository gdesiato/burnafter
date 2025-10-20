package com.burnafter.burnafter.configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.lang.NonNull;

@Component
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    @Value("${app.maxTextBytes:20000}")
    private long maxBytes;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(req.getMethod()) && req.getRequestURI().startsWith("/api/pastes")) {
            long len = req.getContentLengthLong(); // -1 if unknown
            if (len > 0 && len > maxBytes) {
                res.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Payload Too Large");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
