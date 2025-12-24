package com.intermediate.Blog.Application.Exception;


import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String , Object>>  handlerRuntimeException(RuntimeException ex){
        Map<String , Object> body = new HashMap<>();
        body.put("Timestamps" , LocalDateTime.now());
        body.put("Error" , ex.getMessage());
        body.put("Status" , HttpStatus.BAD_REQUEST.value());

        return new  ResponseEntity<>(body , HttpStatus.BAD_REQUEST);

    }
}
