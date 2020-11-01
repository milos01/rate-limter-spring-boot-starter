package com.mandric.ratelimiterspringbootstarter.registy;

import com.mandric.ratelimiterspringbootstarter.provider.Provider;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Data
@Slf4j
@Component
@Scope("singleton")
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterRegistry {

    @Qualifier("constraints")
    private Provider constraints;

    public RSemaphore getSemaphore(final String name) {
        if (checkConstraints(name)) {
            log.warn("No concurrency limit config was found for name \"{}\"", name);
            return null;
        }

        return constraints.getConstraints().get(name.toLowerCase()).getSemaphore();
    }

    public Bucket getBucket(final String name) {
        if (checkConstraints(name)) {
            log.warn("No bucket limit config was found for name \"{}\"", name);
            return null;
        }

        return constraints.getConstraints().get(name.toLowerCase()).getBucket();
    }

    private boolean checkConstraints(String name) {
        return constraints.getConstraints().isEmpty() || Objects.isNull(constraints.getConstraints().get(name.toLowerCase()));
    }
}
