package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ResponseData {

    @Builder.Default
    private final String id = UUID.randomUUID().toString();
    private final String message;
    private final Object data;
    @Builder.Default
    private final Instant createdDate = Instant.now();

}
