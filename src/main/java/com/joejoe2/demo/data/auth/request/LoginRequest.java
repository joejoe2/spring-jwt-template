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
public class LoginRequest {
  @Schema(description = "name of the user")
  @NotEmpty(message = "username cannot be empty !")
  private String username;

  @Schema(description = "password of the user")
  @NotEmpty(message = "password cannot be empty !")
  private String password;
}
