package com.sample.demo.aws.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InternalException extends RuntimeException {
    private final String message;
    private final Integer statusCode;
    private final Exception e;
}
