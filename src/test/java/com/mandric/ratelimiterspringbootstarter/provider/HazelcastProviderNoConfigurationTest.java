package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
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
//@RequiredArgsConstructor
@DisplayName("When running HazelcastBucket")
@TestPropertySource(locations = "classpath:/hazelcast-empty-limits.properties")
@ContextConfiguration(classes = {RateLimiterConfig.class})
class HazelcastProviderNoConfigurationTest {
    @Autowired
    @Qualifier("constraints")
    private Provider constraints;

    @Test
    @DisplayName("Constraints for successful response")
    void testMultipleSuccessfulHazelcastBucketCreation() {
        assertAll(
                "Assert first limit",
                () -> assertEquals(0, constraints.getConstraints().size(), "Should be 0"),
                () -> assertNull(constraints.getConstraints().get("testhazelcastlimit1"))
        );
    }
}
