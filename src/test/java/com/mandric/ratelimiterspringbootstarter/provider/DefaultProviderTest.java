package com.mandric.ratelimiterspringbootstarter.provider;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DisplayName("When running DefaultProvider")
@ContextConfiguration(classes = {RateLimiterConfig.class})
class DefaultProviderTest {

    @Autowired
    @Qualifier("constraints")
    private Provider constraints;

    @Test
    @DisplayName("DefaultBucket constructor for successful response")
    void testMultipleSuccessfulDefaultBucketCreation() {

        assertAll(
                "Assert default bucket",
                () -> assertNotNull(constraints.getConstraints()),
                () -> assertEquals(0, constraints.getConstraints().size(), "Should be 0 always")
        );
    }
}
