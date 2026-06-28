package com.ljlblogserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ArticleSaveRequest {

    @NotBlank(message = "slug 不能为空")
    private String slug;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;

    private String cover;

    @NotBlank(message = "分类 slug 不能为空")
    private String categorySlug;

    private List<String> tags;

    @NotNull(message = "发布日期不能为空")
    private LocalDate publishDate;

    private Integer readTime;

    private Boolean featured;
}
