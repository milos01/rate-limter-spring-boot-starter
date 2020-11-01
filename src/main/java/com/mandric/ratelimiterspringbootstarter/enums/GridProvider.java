package com.mandric.ratelimiterspringbootstarter.enums;

public enum GridProvider {
    JCACHE("JCache"),

    HAZELCAST("Hazelcast");

    private String name;

    private GridProvider(final String name) {
     this.name = name;
    }

    public String getName() {
        return name;
    }
}
