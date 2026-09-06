package com.harsh.uday.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration with profile-specific strategies.
 * - Dev: Simple in-memory (ConcurrentHashMap) - no TTL, fast startup
 * - Prod: Caffeine with TTL and max size to prevent unbounded growth
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        return new ConcurrentMapCacheManager("products", "products-all", "product");
    }

    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products", "products-all", "product");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats());
        return cacheManager;
    }
}
