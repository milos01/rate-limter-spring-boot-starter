package com.mandric.ratelimiterspringbootstarter.service;

import com.mandric.ratelimiterspringbootstarter.exception.ConsumeLimitException;
import com.mandric.ratelimiterspringbootstarter.registy.RateLimiterRegistry;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final int DEFAULT_OPERATION_COST = 1;

    private final RateLimiterRegistry rateLimiterRegistry;

    public boolean limit(String limitName) {
        return limit(limitName, DEFAULT_OPERATION_COST);
    }

    public boolean limit(String limitName, int operationCost) {
        try {
            RSemaphore semaphore = rateLimiterRegistry.getSemaphore(limitName);
            Bucket bucket = rateLimiterRegistry.getBucket(limitName);

            if (Objects.nonNull(semaphore)) {
                //blocking call
                semaphore.acquire();
                log.info("Method limit name: {} - Semaphore acquired permit. Permits left: {}", limitName, semaphore.availablePermits());
            }

            if (Objects.nonNull(bucket)) {
                //blocking call
                bucket.asScheduler().consume(operationCost);
                log.info("Method limit name: {} - {} token/s are being consumed from bucket", limitName, operationCost);
            }
            return true;
        } catch (InterruptedException e) {
            log.error("Error has occurred: ", e);
            throw new ConsumeLimitException("Current thread has been interrupted during the waiting");
        }
    }

    public boolean release(String name) {
        RSemaphore semaphore = rateLimiterRegistry.getSemaphore(name);
        if (Objects.nonNull(semaphore)) {
            semaphore.release();
        }
        return true;
    }


}
