package com.mandric.ratelimiterspringbootstarter.service;

import com.mandric.ratelimiterspringbootstarter.exception.ConsumeLimitException;
import com.mandric.ratelimiterspringbootstarter.provider.ConstraintVault;
import com.mandric.ratelimiterspringbootstarter.provider.DefaultProvider;
import com.mandric.ratelimiterspringbootstarter.provider.Provider;
import com.mandric.ratelimiterspringbootstarter.provider.RedissonProvider;
import com.mandric.ratelimiterspringbootstarter.registy.RateLimiterRegistry;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RSemaphore;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DisplayName("When running RateLimiterService")
@ExtendWith(SpringExtension.class)
class RateLimiterServiceTest {

    @Spy
    RateLimiterRegistry rateLimiterRegistry;

    @Mock
    private RSemaphore semaphore;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Test
    @SneakyThrows
    @DisplayName("Limit method for empty limit list and not limit specified")
    void testLimitWithEmptyLimitListAndNoName() {
        rateLimiterRegistry.setConstraints(generateEmptyLimitList());
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateEmptyLimitList());
        boolean response = rateLimiterService.limit("");
        assertTrue(response);
    }

    @Test
    @DisplayName("Limit method for limit name not found")
    @SneakyThrows
    void testLimitWithLimitNameNotFound() {
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateNotEmptyLimitList());
        rateLimiterRegistry.setConstraints(generateNotEmptyLimitList());

        assertAll(
                "Assert response on not empty limit list",
                () -> assertTrue(rateLimiterService.limit("testkeyonenf")),
                () -> assertTrue(rateLimiterService.limit("testkeytwonf")),
                () -> assertTrue(rateLimiterService.limit("testkeythreenf"))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Limit method for non empty bucket list and found name")
    void testLimitWithLimitNameFound() {
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateNotEmptyLimitList());
        rateLimiterRegistry.setConstraints(generateNotEmptyLimitList());

        assertAll(
                "Assert response on not empty limit list",
                () -> assertTrue(rateLimiterService.limit("testkeyone")),
                () -> assertTrue(rateLimiterService.limit("testkeytwo")),
                () -> assertTrue(rateLimiterService.limit("testkeythree"))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Limit method for empty limit list")
    void testLimitWithEmptyLimitList() {
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateEmptyLimitList());
        rateLimiterRegistry.setConstraints(generateEmptyLimitList());

        assertAll(
                "Assert response on empty limit list",
                () -> assertTrue(rateLimiterService.limit("testkeyone")),
                () -> assertTrue(rateLimiterService.limit("testkeytwo")),
                () -> assertTrue(rateLimiterService.limit("testkeythree"))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Limit method for exception")
    void testLimitWithException() {
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateNotEmptyLimitList());
        doThrow(InterruptedException.class).when(semaphore).acquire();
        rateLimiterRegistry.setConstraints(generateNotEmptyLimitList());

        assertAll(
                "Assert response on exception",
                () -> assertThrows(ConsumeLimitException.class, () -> rateLimiterService.limit("testkeyone")),
                () -> assertTrue(rateLimiterService.limit("testkeytwo")),
                () -> assertThrows(ConsumeLimitException.class, () -> rateLimiterService.limit("testkeythree"))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Limit method for releasing lock")
    void testLimitForReleasingLock() {
        when(rateLimiterRegistry.getConstraints()).thenReturn(generateNotEmptyLimitList());
        rateLimiterRegistry.setConstraints(generateNotEmptyLimitList());

        assertAll(
                "Assert response on empty limit list",
                () -> assertTrue(rateLimiterService.release("testkeyone")),
                () -> assertTrue(rateLimiterService.release("testkeyonenf"))
        );
    }

    private Provider generateNotEmptyLimitList() {
        Map<String, ConstraintVault> bucketList = new HashMap<>();
        Bandwidth bandwidth = Bandwidth.classic(10L, Refill.intervally(10, Duration.ofMinutes(1)));
        bucketList.put("testkeyone", ConstraintVault.builder().semaphore(semaphore).bucket(null).build());
        bucketList.put("testkeytwo", ConstraintVault.builder().semaphore(null).bucket(Bucket4j.builder().addLimit(bandwidth).build()).build());
        bucketList.put("testkeythree", ConstraintVault.builder().semaphore(semaphore).bucket(Bucket4j.builder().addLimit(bandwidth).build()).build());
        RedissonProvider redissonProvider = new RedissonProvider();
        redissonProvider.setConstraints(bucketList);
        return redissonProvider;
    }

    private Provider generateEmptyLimitList() {
        return new DefaultProvider();
    }
}
