package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import io.github.bucket4j.grid.GridBucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DisplayName("When running HazelcastBucket")
@TestPropertySource(locations = "classpath:/hazelcast-limits.properties")
@ContextConfiguration(classes = {RateLimiterConfig.class})
class HazelcastProviderTest {

    @Autowired
    @Qualifier("constraints")
    private Provider constraints;

    @Test
    @DisplayName("Constraints for successful response")
    void testMultipleSuccessfulHazelcastBucketCreation() {
        assertEquals(2, constraints.getConstraints().size(), "Should be 2");
        assertAll(
                "Assert first limit",
                () -> assertNotNull(constraints.getConstraints().get("testhazelcastlimit1")),
                () -> assertTrue(constraints.getConstraints().get("testhazelcastlimit1").getBucket() instanceof GridBucket),
                () -> assertNull(constraints.getConstraints().get("testhazelcastlimit1").getSemaphore())
        );

        assertAll(
                "Assert second limit",
                () -> assertNotNull(constraints.getConstraints().get("testhazelcastlimit2")),
                () -> assertTrue(constraints.getConstraints().get("testhazelcastlimit2").getBucket() instanceof GridBucket),
                () -> assertNull(constraints.getConstraints().get("testhazelcastlimit2").getSemaphore())
        );
    }
}
