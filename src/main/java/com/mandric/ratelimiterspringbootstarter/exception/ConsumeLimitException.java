package com.mandric.ratelimiterspringbootstarter.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ConsumeLimitException extends RuntimeException {
    private static final long serialVersionUID = -4257427895824088584L;
    private String message;
}
