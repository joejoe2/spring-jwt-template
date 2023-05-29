package com.joejoe2.demo.data.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RefreshTokenSpec {
  @Schema(description = "expiration date in number of seconds since Epoch")
  long exp;

  @Schema(description = "issuer")
  String iss;

  @Schema(description = "access token id")
  String jti;

  @Schema(description = "token type", example = "refresh_token")
  String type;
}
