package com.ljlblogserver.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Article {

    private Long id;
    private String articleType;
    private String slug;
    private String title;
    private String summary;
    private String coverPath;
    private String contentPath;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private LocalDate publishDate;
    private Integer readTime;
    private Boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags = new ArrayList<>();
}
