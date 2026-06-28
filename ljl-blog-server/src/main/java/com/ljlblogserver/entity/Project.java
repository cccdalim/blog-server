package com.ljlblogserver.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Project {

    private Long id;
    private String name;
    private String description;
    private String summary;
    private String cover;
    private String gitUrl;
    private String demoUrl;
    private Boolean featured;
    private LocalDate startDate;
    private LocalDate endDate;
    private String techStackJson;
    private String devProcessJson;
    private String screenshotsJson;
    private String timelineJson;
    private LocalDateTime createdAt;
}
