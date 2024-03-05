package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.Role;
import com.joejoe2.demo.validation.constraint.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** request for change the role of target user */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {
  @Schema(description = "id of target user")
  @UUID(message = "invalid user id !")
  @NotEmpty(message = "user id cannot be empty !")
  private String id;

  @Schema(description = "the role that target user want to change to")
  @Role
  private String role;
}
