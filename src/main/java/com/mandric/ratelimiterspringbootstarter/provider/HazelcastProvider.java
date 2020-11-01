package com.mandric.ratelimiterspringbootstarter.provider;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import io.github.bucket4j.grid.hazelcast.HazelcastBucketBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.Collections;
import java.util.stream.Collectors;

@ConditionalOnClass(Hazelcast.class)
public class HazelcastProvider extends Provider {
    public HazelcastProvider(RateLimiterConfig rateLimiterConfig) {

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
                .semaphore(null)
                .bucket(buildBucket(limit, rateLimiterConfig))
                .build();
    }

    private Bucket buildBucket (RateLimiterConfig.SingleLimitConfig limit, RateLimiterConfig rateLimiterConfig) {
        final HazelcastInstance hzInstance = com.hazelcast.core.Hazelcast.newHazelcastInstance();
        final IMap<String, GridBucketState> map = hzInstance.getMap(rateLimiterConfig.getName());
        HazelcastBucketBuilder bucketBuilder = Bucket4j.extension(io.github.bucket4j.grid.hazelcast.Hazelcast.class).builder();
        addLimits(limit, bucketBuilder);
        return bucketBuilder.build(map, limit.getName(), RecoveryStrategy.RECONSTRUCT);
    }
}
