package com.mandric.ratelimiterspringbootstarter.registry;

import com.mandric.ratelimiterspringbootstarter.provider.ConstraintVault;
import com.mandric.ratelimiterspringbootstarter.provider.DefaultProvider;
import com.mandric.ratelimiterspringbootstarter.provider.Provider;
import com.mandric.ratelimiterspringbootstarter.provider.RedissonProvider;
import com.mandric.ratelimiterspringbootstarter.registy.RateLimiterRegistry;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LockFreeBucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.redisson.Redisson;
import org.redisson.RedissonSemaphore;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running RateLimiterRegistry")
@ExtendWith(SpringExtension.class)
class RateLimiterRegistryTest {

    @InjectMocks
    private RateLimiterRegistry rateLimiterRegistry;

    @Test
    @DisplayName("GetBucket & getSemaphore method for not empty constraints list")
    void testSuccessfulRateLimiterRegistry() {

        rateLimiterRegistry.setConstraints(generateLimitList());
        assertAll(
                () -> assertNotNull(rateLimiterRegistry.getConstraints()),
                () -> assertEquals(3, rateLimiterRegistry.getConstraints().getConstraints().size(), "Should be 3")
        );

        assertAll(
                "Assert default not null semaphore with null bucket",
                () -> assertNotNull(rateLimiterRegistry.getSemaphore("testkeyone")),
                () -> assertNull(rateLimiterRegistry.getBucket("testkeyone")),
                () -> assertTrue(rateLimiterRegistry.getSemaphore("testkeyone") instanceof RedissonSemaphore)
        );

        assertAll(
                "Assert default null semaphore with not null bucket",
                () -> assertNull(rateLimiterRegistry.getSemaphore("testkeytwo")),
                () -> assertNotNull(rateLimiterRegistry.getBucket("testkeytwo")),
                () -> assertTrue(rateLimiterRegistry.getBucket("testkeytwo") instanceof LockFreeBucket)
        );

        assertAll(
                "Assert default not null semaphore with not null bucket",
                () -> assertNotNull(rateLimiterRegistry.getSemaphore("testkeythree")),
                () -> assertNotNull(rateLimiterRegistry.getSemaphore("testkeythree")),
                () -> assertNotNull(rateLimiterRegistry.getBucket("testkeythree")),
                () -> assertNotNull(rateLimiterRegistry.getBucket("testkeythree")),
                () -> assertTrue(rateLimiterRegistry.getSemaphore("testkeythree") instanceof RedissonSemaphore),
                () -> assertEquals("test", rateLimiterRegistry.getSemaphore("testkeythree").getName(), "Name should be test"),
                () -> assertTrue(rateLimiterRegistry.getBucket("testkeythree") instanceof LockFreeBucket),
                () -> assertEquals(10, rateLimiterRegistry.getBucket("testkeythree").getAvailableTokens(), "Should be 10")
        );
    }

    @Test
    @DisplayName("GetBucket & getSemaphore method for empty constraints list")
    void testSuccessfulEmptyRateLimiterRegistry() {

        rateLimiterRegistry.setConstraints(generateEmptyBucketList());

        assertAll(
                "Assert default lock free empty bucket bucket",
                () -> assertNotNull(rateLimiterRegistry.getConstraints()),
                () -> assertNull(rateLimiterRegistry.getBucket("nill"))
        );
    }

    private Provider generateLimitList() {
        Map<String, ConstraintVault> bucketList = new HashMap<>();
        Bandwidth bandwidth = Bandwidth.classic(10L, Refill.intervally(10, Duration.ofMinutes(1)));
        bucketList.put("testkeyone", ConstraintVault.builder().semaphore(buildSemaphore()).bucket(null).build());
        bucketList.put("testkeytwo", ConstraintVault.builder().semaphore(null).bucket(Bucket4j.builder().addLimit(bandwidth).build()).build());
        bucketList.put("testkeythree", ConstraintVault.builder().semaphore(buildSemaphore()).bucket(Bucket4j.builder().addLimit(bandwidth).build()).build());
        RedissonProvider redissonProvider = new RedissonProvider();
        redissonProvider.setConstraints(bucketList);
        return redissonProvider;
    }

    private RSemaphore buildSemaphore () {
        final RedissonClient client = Redisson.create();
        return client.getSemaphore("test");
    }

    private Provider generateEmptyBucketList() {
        return new DefaultProvider();
    }
}
