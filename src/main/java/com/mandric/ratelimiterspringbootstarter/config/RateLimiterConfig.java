package com.mandric.ratelimiterspringbootstarter.config;

import com.mandric.ratelimiterspringbootstarter.enums.GridProvider;
import com.mandric.ratelimiterspringbootstarter.enums.LimitType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Data
@ComponentScan(basePackages = "com.mandric.ratelimiterspringbootstarter")
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterConfig {
    private Map<String, String> limits;
    private String name;
    private GridProvider provider;
    private List<SingleLimitConfig> limitConfigs;

    @Data
    public static class SingleLimitConfig {
        private String name;
        private List<LimitConstraint> limits;
    }

    @Data
    public static class LimitConstraint {
        private int amount;
        private ChronoUnit unit;
        private LimitType type;
    }
}
