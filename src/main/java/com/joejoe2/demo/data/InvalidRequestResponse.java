package com.joejoe2.demo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;
import java.util.TreeSet;

@Data
public class InvalidRequestResponse {
    @Schema(description = "errors in each field of request payload",
            title = "errors in each field of request payload")
    Map<String, TreeSet<String>> errors;

    public InvalidRequestResponse(Map<String, TreeSet<String>> errors) {
        this.errors = errors;
    }
}
