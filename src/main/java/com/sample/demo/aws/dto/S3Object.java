package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class S3Object {
    private final String key;
    private final String eTag;
    private final Long size;
    private final String storageClass;
}
