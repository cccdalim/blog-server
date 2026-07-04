package com.ljlblogserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetaSaveRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "slug 不能为空")
    private String slug;

    /** content（Blog/Docs）或 recipe（菜谱），默认 content */
    private String scope;
}
