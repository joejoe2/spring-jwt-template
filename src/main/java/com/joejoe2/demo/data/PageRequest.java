package com.joejoe2.demo.data;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
  @Parameter(description = "must >= 0")
  @Min(value = 0, message = "page must >= 0 !")
  @NotNull(message = "page is missing !")
  private Integer page;

  @Parameter(description = "must >= 0")
  @Min(value = 1, message = "page must >= 1 !")
  @NotNull(message = "size is missing !")
  private Integer size;
}
