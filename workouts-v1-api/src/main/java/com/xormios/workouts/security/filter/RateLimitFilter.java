package com.xormios.workouts.security.filter;

import com.xormios.workouts.security.dto.ErrorResponse;
import com.xormios.workouts.security.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket;
        if ("POST".equals(method) && "/api/v1/auth/login".equals(uri)) {
            bucket = rateLimiterService.resolveLoginBucket(request.getRemoteAddr());
        } else if ("POST".equals(method) && "/api/v1/auth/register".equals(uri)) {
            bucket = rateLimiterService.resolveRegisterBucket(request.getRemoteAddr());
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
        if (consumptionProbe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = consumptionProbe.getNanosToWaitForRefill() / 1_000_000_000;
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse("rate_limit_exceeded", "Too many requests. Please try again later."));
    }
}
