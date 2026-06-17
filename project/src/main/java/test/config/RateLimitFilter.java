package test.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Einfacher, In-Memory basierter Rate-Limiter fuer den Login-Endpoint.
 *
 * Zaehlt Requests pro Client-IP innerhalb eines Zeitfensters. Wird das
 * Limit ueberschritten, antwortet der Filter mit HTTP 429 (Too Many Requests).
 *
 * false -> verwundbar: kein Limit, beliebig viele Login-Versuche moeglich (Brute-Force)
 * true  -> sicher: max. maxRequests Versuche
 *
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limiting:true}")
    private boolean rateLimitingEnabled;

    @Value("${app.security.rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${app.security.rate-limit.window-ms:60000}")
    private long windowMs;

    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!rateLimitingEnabled || !"/auth/login".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        RequestWindow window = requestCounts.computeIfAbsent(clientIp, ip -> new RequestWindow(now));

        synchronized (window) {
            if (now - window.windowStart > windowMs) {
                // neues Zeitfenster beginnt
                window.windowStart = now;
                window.count.set(0);
            }

            int currentCount = window.count.incrementAndGet();

            if (currentCount > maxRequests) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Too many login attempts. Please try again later.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestWindow {
        volatile long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        RequestWindow(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
