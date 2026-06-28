package com.ljlblogserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PhotoSaveRequest {

    @NotBlank(message = "图片 URL 不能为空")
    private String url;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String city;
    private String country;
    private String category;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    private Integer width;
    private Integer height;
}
