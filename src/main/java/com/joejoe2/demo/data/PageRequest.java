package com.joejoe2.demo.data;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PageRequest {
    @Min(value = 0, message = "page is at least 0 !")
    @NotNull(message = "page is missing !")
    private Integer page;

    @Min(value = 1, message = "page is at least 1 !")
    @NotNull(message = "size is missing !")
    private Integer size;
}
