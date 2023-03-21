package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** request with target user id */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdRequest {
  @Schema(description = "id of target user")
  @UUID(message = "invalid user id !")
  @NotEmpty(message = "user id cannot be empty !")
  private String id;
}
