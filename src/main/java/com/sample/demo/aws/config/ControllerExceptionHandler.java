package com.sample.demo.aws.config;

import com.sample.demo.aws.dto.ResponseData;
import com.sample.demo.aws.exception.InternalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseData> noHandlerFoundException(NoHandlerFoundException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ResponseData> internalException(InternalException e) {
        return ResponseEntity.status(e.getStatusCode()).body(ResponseData.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData> unknowException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.builder().message(e.getMessage()).build());
    }
}
