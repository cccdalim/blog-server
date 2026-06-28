package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.ProjectDto;
import com.ljlblogserver.dto.ProjectNavDto;
import com.ljlblogserver.dto.ProjectSaveRequest;
import com.ljlblogserver.service.ProjectSeedService;
import com.ljlblogserver.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectSeedService projectSeedService;

    @GetMapping("/list")
    public ApiResponse<PageResult<ProjectDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean featured) {
        return ApiResponse.success(projectService.list(keyword, featured, page, pageSize));
    }

    @GetMapping("/featured")
    public ApiResponse<List<ProjectDto>> featured(@RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.success(projectService.featured(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDto> detail(@PathVariable String id) {
        return ApiResponse.success(projectService.getById(id));
    }

    @GetMapping("/{id}/nav")
    public ApiResponse<ProjectNavDto> nav(@PathVariable String id) {
        return ApiResponse.success(projectService.getNav(id));
    }

    @PostMapping
    public ApiResponse<ProjectDto> create(@Valid @RequestBody ProjectSaveRequest request) {
        return ApiResponse.success(projectService.create(request), "创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectDto> update(@PathVariable String id, @Valid @RequestBody ProjectSaveRequest request) {
        return ApiResponse.success(projectService.update(id, request), "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Integer>> seed(@RequestParam(defaultValue = "false") boolean replace)
            throws Exception {
        int count = projectSeedService.importSeed(replace);
        return ApiResponse.success(Map.of("count", count), count > 0 ? "导入成功" : "已有数据，未导入");
    }
}
