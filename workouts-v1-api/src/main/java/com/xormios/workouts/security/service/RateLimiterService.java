package com.xormios.workouts.security.service;

import com.xormios.workouts.security.config.RateLimiterProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class RateLimiterService {

    private final RateLimiterProperties rateLimiterProperties;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    public Bucket resolveLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, key -> newLoginBucket());
    }

    public Bucket resolveRegisterBucket(String ip) {
        return registerBuckets.computeIfAbsent(ip, key -> newRegisterBucket());
    }

    private Bucket newLoginBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimiterProperties.getLoginCapacity())
                .refillIntervally(rateLimiterProperties.getLoginCapacity(), Duration.ofSeconds(rateLimiterProperties.getLoginRefillPeriodSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newRegisterBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimiterProperties.getRegisterCapacity())
                .refillIntervally(rateLimiterProperties.getRegisterCapacity(), Duration.ofSeconds(rateLimiterProperties.getRegisterRefillPeriodSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
