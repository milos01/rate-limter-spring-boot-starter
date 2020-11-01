package com.mandric.ratelimiterspringbootstarter.util;

public class RateLimiterUtils {
    public static String getSemaphoreNameFormat(String name) {
        return String.format("%s_semaphore", name.toLowerCase());
    }

    public static String getBucketNameFormat(String name) {
        return String.format("%s_bucket", name.toLowerCase());
    }
}
