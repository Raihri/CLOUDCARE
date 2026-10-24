package com.example.cloud.care.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * CSRF defence by verifying the request's origin (OWASP "verifying origin with
 * standard headers").
 *
 * <p>Browsers attach {@code Origin} (or at least {@code Referer}) to every
 * cross-site state-changing request, and a page cannot forge it. So for
 * POST/PUT/PATCH/DELETE we reject any request whose origin is not one of the
 * configured CloudCare origins. Requests with neither header are not from a
 * browser page (e.g. the Next.js server calling the API server-to-server) and
 * carry no ambient-credential risk, so they pass.
 *
 * <p>This sits alongside {@code SameSite=Lax} session cookies and Next.js'
 * own Origin check on Server Actions, which is why Spring's token-based CSRF
 * is not also enabled: the frontend calls the API from the server, where a
 * synchronizer token would add a round trip without adding protection.
 */
public class OriginCheckFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OriginCheckFilter.class);
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final Set<String> allowedOrigins;

    public OriginCheckFilter(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (SAFE_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String origin = request.getHeader("Origin");
        if (origin == null) {
            origin = originOf(request.getHeader("Referer"));
        }

        if (origin != null && !allowedOrigins.contains(origin)) {
            log.warn("Blocked cross-origin {} {} from {}", request.getMethod(), request.getRequestURI(), origin);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-origin request blocked");
            return;
        }
        chain.doFilter(request, response);
    }

    /** "https://host:port/path" -> "https://host:port"; null when absent or unparsable. */
    static String originOf(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) return "invalid";
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        } catch (IllegalArgumentException e) {
            return "invalid";
        }
    }
}
