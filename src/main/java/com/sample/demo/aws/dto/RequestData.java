package com.sample.demo.aws.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

import java.time.Instant;

@Data
public class RequestData {
    @JsonProperty("id")
    private String id;

    @JsonProperty("user")
    private String user;

    @JsonProperty("value")
    private String value;

    @JsonProperty("createdDate")
    private Instant createdDate;
    public RequestData() {

    }
}
