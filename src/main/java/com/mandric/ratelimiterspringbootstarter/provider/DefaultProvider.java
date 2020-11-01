package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;

import java.util.Collections;

@ConditionalOnMissingClass
public class DefaultProvider extends Provider {

    public DefaultProvider() {
        constraints = Collections.emptyMap();
    }

    @Override
    ConstraintVault buildConstraints(RateLimiterConfig.SingleLimitConfig limit, RateLimiterConfig rateLimiterConfig) {
        return null;
    }
}
