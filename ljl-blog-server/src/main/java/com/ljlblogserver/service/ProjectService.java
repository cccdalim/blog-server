package com.ljlblogserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.ProjectDto;
import com.ljlblogserver.dto.ProjectNavDto;
import com.ljlblogserver.dto.ProjectNavItemDto;
import com.ljlblogserver.dto.ProjectSaveRequest;
import com.ljlblogserver.dto.ProjectTimelineDto;
import com.ljlblogserver.entity.Project;
import com.ljlblogserver.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;
    private final ManagedUploadCleanupService uploadCleanupService;

    public PageResult<ProjectDto> list(String keyword, Boolean featured, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<Project> projects = projectMapper.findPage(keyword, featured, offset, pageSize);
        long total = projectMapper.count(keyword, featured);
        return new PageResult<>(projects.stream().map(this::toSummaryDto).toList(), total, page, pageSize);
    }

    public List<ProjectDto> featured(int limit) {
        return projectMapper.findFeatured(limit).stream().map(this::toSummaryDto).toList();
    }

    public ProjectDto getById(String id) {
        Project project = findProjectOrNull(id);
        return project == null ? null : toDetailDto(project);
    }

    public ProjectNavDto getNav(String id) {
        List<Project> sorted = projectMapper.findAllSorted();
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (String.valueOf(sorted.get(i).getId()).equals(id)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return new ProjectNavDto(null, null);
        }
        ProjectNavItemDto prev = index > 0
                ? toNavItem(sorted.get(index - 1))
                : null;
        ProjectNavItemDto next = index < sorted.size() - 1
                ? toNavItem(sorted.get(index + 1))
                : null;
        return new ProjectNavDto(prev, next);
    }

    @Transactional
    public ProjectDto create(ProjectSaveRequest request) {
        Project project = fromRequest(request);
        projectMapper.insert(project);
        return toDetailDto(project);
    }

    @Transactional
    public ProjectDto update(String id, ProjectSaveRequest request) {
        Project existing = requireProject(id);
        List<String> oldScreenshots = readStringList(existing.getScreenshotsJson());
        uploadCleanupService.cleanupRemovedProjectAssets(
                existing.getCover(),
                oldScreenshots,
                request.getCover(),
                request.getScreenshots(),
                existing.getId());

        Project project = fromRequest(request);
        project.setId(existing.getId());
        int rows = projectMapper.update(project);
        if (rows == 0) {
            throw new BusinessException(500, "项目更新失败，请刷新后重试");
        }
        return toDetailDto(projectMapper.findById(existing.getId()));
    }

    @Transactional
    public void delete(String id) {
        Project existing = requireProject(id);
        uploadCleanupService.deleteProjectAssets(
                existing.getCover(),
                readStringList(existing.getScreenshotsJson()));
        projectMapper.deleteById(existing.getId());
    }

    private Project requireProject(String id) {
        Project project = findProjectOrNull(id);
        if (project == null) {
            throw BusinessException.notFound("项目不存在");
        }
        return project;
    }

    private Project fromRequest(ProjectSaveRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setSummary(request.getSummary());
        project.setCover(request.getCover());
        project.setGitUrl(request.getGitUrl());
        project.setDemoUrl(request.getDemoUrl());
        project.setFeatured(Boolean.TRUE.equals(request.getFeatured()));
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setTechStackJson(writeStringList(request.getTechStack()));
        project.setDevProcessJson(writeStringList(request.getDevProcess()));
        project.setScreenshotsJson(writeStringList(request.getScreenshots()));
        project.setTimelineJson(writeTimeline(request.getTimeline()));
        return project;
    }

    private String writeStringList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values != null ? values : List.of());
        } catch (Exception ex) {
            return "[]";
        }
    }

    private String writeTimeline(List<ProjectTimelineDto> timeline) {
        try {
            return objectMapper.writeValueAsString(timeline != null ? timeline : List.of());
        } catch (Exception ex) {
            return "[]";
        }
    }

    private Project findProjectOrNull(String id) {
        Long projectId;
        try {
            projectId = Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
        return projectMapper.findById(projectId);
    }

    private ProjectNavItemDto toNavItem(Project project) {
        return new ProjectNavItemDto(String.valueOf(project.getId()), project.getName());
    }

    private ProjectDto toSummaryDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(String.valueOf(project.getId()));
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setSummary(project.getSummary());
        dto.setTechStack(readStringList(project.getTechStackJson()));
        dto.setCover(project.getCover());
        dto.setGitUrl(project.getGitUrl());
        dto.setDemoUrl(project.getDemoUrl());
        dto.setFeatured(project.getFeatured());
        dto.setStartDate(project.getStartDate() != null ? project.getStartDate().toString() : null);
        dto.setEndDate(project.getEndDate() != null ? project.getEndDate().toString() : null);
        return dto;
    }

    private ProjectDto toDetailDto(Project project) {
        ProjectDto dto = toSummaryDto(project);
        dto.setDevProcess(readStringList(project.getDevProcessJson()));
        dto.setScreenshots(readStringList(project.getScreenshotsJson()));
        dto.setTimeline(readTimeline(project.getTimelineJson()));
        return dto;
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private List<ProjectTimelineDto> readTimeline(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
