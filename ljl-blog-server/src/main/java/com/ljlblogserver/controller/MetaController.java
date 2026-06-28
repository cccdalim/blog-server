package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.dto.CategoryDto;
import com.ljlblogserver.dto.MetaSaveRequest;
import com.ljlblogserver.dto.TagDto;
import com.ljlblogserver.service.MetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.success(metaService.categories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<TagDto>> tags() {
        return ApiResponse.success(metaService.tags());
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryDto> createCategory(@Valid @RequestBody MetaSaveRequest request) {
        return ApiResponse.success(metaService.createCategory(request), "创建成功");
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryDto> updateCategory(@PathVariable String id,
                                                   @Valid @RequestBody MetaSaveRequest request) {
        return ApiResponse.success(metaService.updateCategory(id, request), "更新成功");
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        metaService.deleteCategory(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/tags")
    public ApiResponse<TagDto> createTag(@Valid @RequestBody MetaSaveRequest request) {
        return ApiResponse.success(metaService.createTag(request), "创建成功");
    }

    @PutMapping("/tags/{id}")
    public ApiResponse<TagDto> updateTag(@PathVariable String id, @Valid @RequestBody MetaSaveRequest request) {
        return ApiResponse.success(metaService.updateTag(id, request), "更新成功");
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable String id) {
        metaService.deleteTag(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/tags/cleanup-orphans")
    public ApiResponse<Map<String, Integer>> cleanupOrphanTags() {
        int count = metaService.cleanupOrphanTags();
        return ApiResponse.success(Map.of("count", count), count > 0 ? "清理完成" : "没有可清理的标签");
    }
}
