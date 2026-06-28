package com.ljlblogserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleDto {

    private String id;
    private String slug;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private String category;
    private String categorySlug;
    private List<String> tags;
    private String publishDate;
    private Integer readTime;
    private Boolean featured;
}
