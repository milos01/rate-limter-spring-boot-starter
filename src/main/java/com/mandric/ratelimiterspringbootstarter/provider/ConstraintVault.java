package com.mandric.ratelimiterspringbootstarter.provider;

import io.github.bucket4j.Bucket;
import lombok.Builder;
import lombok.Data;
import org.redisson.api.RSemaphore;

@Data
@Builder
public class ConstraintVault {
    private RSemaphore semaphore;
    private Bucket bucket;
}
