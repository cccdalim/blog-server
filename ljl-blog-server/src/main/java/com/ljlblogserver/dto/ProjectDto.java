package com.ljlblogserver.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectDto {

    private String id;
    private String name;
    private String description;
    private String summary;
    private List<String> techStack = new ArrayList<>();
    private String cover;
    private String gitUrl;
    private String demoUrl;
    private Boolean featured;
    private String startDate;
    private String endDate;
    private List<String> devProcess = new ArrayList<>();
    private List<String> screenshots = new ArrayList<>();
    private List<ProjectTimelineDto> timeline = new ArrayList<>();
}
