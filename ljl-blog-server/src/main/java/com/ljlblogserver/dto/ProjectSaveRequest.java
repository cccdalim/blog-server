package com.ljlblogserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectSaveRequest {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "摘要不能为空")
    private String summary;

    private String cover;

    private String gitUrl;

    private String demoUrl;

    private Boolean featured;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> techStack;

    private List<String> devProcess;

    private List<String> screenshots;

    private List<ProjectTimelineDto> timeline;
}
