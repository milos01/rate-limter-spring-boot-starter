package com.mandric.ratelimiterspringbootstarter;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import com.mandric.ratelimiterspringbootstarter.provider.DefaultProvider;
import com.mandric.ratelimiterspringbootstarter.provider.HazelcastProvider;
import com.mandric.ratelimiterspringbootstarter.provider.RedissonProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RateLimiterConfig.class})
public class RateLimiterAutoConfiguration {

    @Bean(name = "constraints")
    @ConditionalOnProperty(
            name = "rate-limiter.provider",
            havingValue = "jcache")
    public RedissonProvider jcacheConstraints(RateLimiterConfig rateLimiterConfig) {
        return new RedissonProvider(rateLimiterConfig);
    }

    @Bean(name = "constraints")
    @ConditionalOnProperty(
            name = "rate-limiter.provider",
            havingValue = "hazelcast")
    public HazelcastProvider hazelcastConstraints(RateLimiterConfig rateLimiterConfig){
        return new HazelcastProvider(rateLimiterConfig);
    }

    @Bean(name = "constraints")
    public DefaultProvider defaultConstraints(){
        return new DefaultProvider();
    }
}
