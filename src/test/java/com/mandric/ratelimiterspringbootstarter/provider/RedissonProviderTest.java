package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import io.github.bucket4j.grid.GridBucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.RedissonSemaphore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DisplayName("When running RedissonBucket")
@TestPropertySource(locations = "classpath:/redisson-limits.properties")
@ContextConfiguration(classes = {RateLimiterConfig.class})
class RedissonProviderTest {

    @Autowired
    @Qualifier("constraints")
    private Provider constraints;

    @Test
    @DisplayName("Constraints for successful response")
    void testMultipleSuccessfulHazelcastBucketCreation() {
        assertEquals(4, constraints.getConstraints().size(), "Should be 4");
        assertAll(
                "Assert first limit",
                () -> assertNotNull(constraints.getConstraints().get("testredissonlimit1")),
                () -> assertTrue(constraints.getConstraints().get("testredissonlimit1").getBucket() instanceof GridBucket),
                () -> assertTrue(constraints.getConstraints().get("testredissonlimit1").getSemaphore() instanceof RedissonSemaphore)
        );

        assertAll(
                "Assert second limit",
                () -> assertNotNull(constraints.getConstraints().get("testredissonlimit2")),
                () -> assertTrue(constraints.getConstraints().get("testredissonlimit2").getBucket() instanceof GridBucket),
                () -> assertTrue(constraints.getConstraints().get("testredissonlimit2").getSemaphore() instanceof RedissonSemaphore)
        );
    }
}
