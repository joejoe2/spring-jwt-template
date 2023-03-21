package com.joejoe2.demo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ErrorMessageResponse {
  @Schema(description = "error message")
  String message;

  public ErrorMessageResponse(String message) {
    this.message = message;
  }
}
