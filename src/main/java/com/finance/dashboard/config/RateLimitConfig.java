package com.finance.dashboard.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    // One bucket per user email — each user gets their own rate limit
    // ConcurrentHashMap is thread-safe — multiple requests can come in simultaneously
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String userEmail) {
        // computeIfAbsent: only creates a new bucket if one doesn't exist for this user
        return buckets.computeIfAbsent(userEmail, this::newBucket);
    }

    private Bucket newBucket(String userEmail) {
        // Allow 60 requests per minute per user
        // Refill.greedy means tokens are added continuously, not all at once
        Bandwidth limit = Bandwidth.classic(60,
                Refill.greedy(60, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}