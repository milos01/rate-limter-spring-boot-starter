package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import com.mandric.ratelimiterspringbootstarter.enums.LimitType;
import com.mandric.ratelimiterspringbootstarter.util.RateLimiterUtils;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import io.github.bucket4j.grid.jcache.JCacheBucketBuilder;
import lombok.NoArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.spi.CachingProvider;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;


@NoArgsConstructor
@ConditionalOnClass(org.redisson.jcache.JCache.class)
public class RedissonProvider extends Provider {

    public RedissonProvider(RateLimiterConfig rateLimiterConfig) {

        if (rateLimiterConfig.getLimitConfigs() == null) {
            constraints = Collections.emptyMap();
            return;
        }

        constraints = rateLimiterConfig.getLimitConfigs().stream().collect(Collectors.toMap(limit -> limit.getName().toLowerCase(),
                limit -> buildConstraints(limit, rateLimiterConfig)));
    }

    @Override
    ConstraintVault buildConstraints(RateLimiterConfig.SingleLimitConfig limit, RateLimiterConfig rateLimiterConfig) {
        return ConstraintVault.builder()
                .semaphore(buildSemaphore(limit))
                .bucket(buildBucket(getOrCreateCache(RateLimiterUtils.getBucketNameFormat(rateLimiterConfig.getName())), limit))
                .build();
    }

    private RSemaphore buildSemaphore(RateLimiterConfig.SingleLimitConfig limit) {
        Optional<RateLimiterConfig.LimitConstraint> hasConcurrencyConstraint = limit.getLimits().stream().filter(constraint -> LimitType.CONCURRENT == constraint.getType()).findFirst();
        if (!hasConcurrencyConstraint.isPresent()) {
            return null;
        }

        final RedissonClient client = Redisson.create();
        final RSemaphore semaphore = client.getSemaphore(RateLimiterUtils.getSemaphoreNameFormat(limit.getName().toLowerCase()));
        semaphore.trySetPermits(hasConcurrencyConstraint.get().getAmount());
        return semaphore;
    }

    private Bucket buildBucket(Cache<String, GridBucketState> cache, RateLimiterConfig.SingleLimitConfig limit) {
        boolean hasTimeConstraint = limit.getLimits().stream().anyMatch(constraint -> LimitType.TIME == constraint.getType());
        if (!hasTimeConstraint) {
            return null;
        }

        JCacheBucketBuilder bucketBuilder = Bucket4j.extension(io.github.bucket4j.grid.jcache.JCache.class).builder();
        addLimits(limit, bucketBuilder);
        return bucketBuilder.build(cache, limit.getName().toLowerCase(), RecoveryStrategy.RECONSTRUCT);
    }

    private Cache<String, GridBucketState> getOrCreateCache(String bucketName) {
        final CachingProvider provider = Caching.getCachingProvider("org.redisson.jcache.JCachingProvider");
        final CacheManager manager = provider.getCacheManager();
        final Cache<String, GridBucketState> foundCache = manager.getCache(bucketName);
        if (foundCache != null) {
            return foundCache;
        }

        MutableConfiguration<String, GridBucketState> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setExpiryPolicyFactory(EternalExpiryPolicy.factoryOf());
        return manager.createCache(bucketName, cacheConfig);
    }
}
