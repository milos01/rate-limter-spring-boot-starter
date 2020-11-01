package com.mandric.ratelimiterspringbootstarter.enums;

public enum LimitType {
    CONCURRENT("Concurrent"),

    TIME("Time");

    private String name;

    private LimitType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
