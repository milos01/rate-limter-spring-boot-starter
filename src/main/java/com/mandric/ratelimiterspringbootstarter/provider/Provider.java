package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import com.mandric.ratelimiterspringbootstarter.enums.LimitType;
import io.github.bucket4j.AbstractBucketBuilder;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

@Data
public abstract class Provider {
    Map<String, ConstraintVault> constraints;

    abstract ConstraintVault buildConstraints(RateLimiterConfig.SingleLimitConfig limit, RateLimiterConfig rateLimiterConfig);

    void addLimits(RateLimiterConfig.SingleLimitConfig limit, AbstractBucketBuilder bucketBuilder) {
        limit.getLimits().stream().filter(constraint -> constraint.getType() != LimitType.CONCURRENT).forEach(constraint -> {
            Bandwidth bandwidth = Bandwidth.classic(constraint.getAmount(), Refill.intervally(constraint.getAmount(), Duration.of(1, constraint.getUnit())));
            bucketBuilder.addLimit(bandwidth);
        });
    }
}
