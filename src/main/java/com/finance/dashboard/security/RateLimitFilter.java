package com.finance.dashboard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.config.RateLimitConfig;
import com.finance.dashboard.dto.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;
    // ObjectMapper converts Java objects to JSON — used to write error response

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only rate-limit authenticated requests — unauthenticated ones are handled by auth
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() ||
                auth.getPrincipal().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userEmail = auth.getName();
        Bucket bucket = rateLimitConfig.resolveBucket(userEmail);

        // tryConsume(1) tries to take 1 token from the bucket
        // If bucket has tokens: request goes through
        // If bucket is empty: rate limit exceeded
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add header so client knows how many requests they have left
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded — return 429 Too Many Requests
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(waitSeconds));

            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Too many requests. Please wait " + waitSeconds + " seconds.");
            response.getWriter().write(
                    objectMapper.writeValueAsString(errorResponse));
        }
    }
}