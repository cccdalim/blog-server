package com.ljlblogserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetaSaveRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "slug 不能为空")
    private String slug;
}
