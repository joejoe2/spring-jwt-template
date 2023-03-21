package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Password;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
  @NotEmpty(message = "password cannot be empty !")
  String oldPassword;

  @Password String newPassword;
}
