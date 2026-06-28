package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.AlbumTimelineDto;
import com.ljlblogserver.dto.FilterOptionDto;
import com.ljlblogserver.dto.PhotoDto;
import com.ljlblogserver.dto.PhotoSaveRequest;
import com.ljlblogserver.service.AlbumSeedService;
import com.ljlblogserver.service.PhotoService;
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
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final PhotoService photoService;
    private final AlbumSeedService albumSeedService;

    @GetMapping("/list")
    public ApiResponse<PageResult<PhotoDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city) {
        return ApiResponse.success(photoService.list(category, country, city, page, pageSize));
    }

    @GetMapping("/recent")
    public ApiResponse<List<PhotoDto>> recent(@RequestParam(defaultValue = "4") int limit) {
        return ApiResponse.success(photoService.recent(limit));
    }

    @GetMapping("/filters")
    public ApiResponse<Map<String, List<FilterOptionDto>>> filters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country) {
        return ApiResponse.success(photoService.filters(category, country));
    }

    @GetMapping("/timeline")
    public ApiResponse<List<AlbumTimelineDto>> timeline(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city) {
        return ApiResponse.success(photoService.timeline(category, country, city));
    }

    @PostMapping
    public ApiResponse<PhotoDto> create(@Valid @RequestBody PhotoSaveRequest request) {
        return ApiResponse.success(photoService.create(request), "创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<PhotoDto> update(@PathVariable String id, @Valid @RequestBody PhotoSaveRequest request) {
        return ApiResponse.success(photoService.update(id, request), "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        photoService.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Integer>> seed(@RequestParam(defaultValue = "false") boolean replace)
            throws Exception {
        int count = albumSeedService.importSeed(replace);
        return ApiResponse.success(Map.of("count", count), count > 0 ? "导入成功" : "已有数据，未导入");
    }
}
