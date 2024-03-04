package com.joejoe2.demo.data.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
  @Schema(description = "refresh token")
  @NotEmpty(message = "refresh token cannot be empty !")
  private String token;
}
