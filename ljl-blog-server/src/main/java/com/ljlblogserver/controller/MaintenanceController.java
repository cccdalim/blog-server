package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.dto.OrphanImageCleanupDto;
import com.ljlblogserver.dto.OrphanImageScanDto;
import com.ljlblogserver.service.ManagedUploadCleanupService;
import com.ljlblogserver.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final ManagedUploadCleanupService uploadCleanupService;
    private final MetaService metaService;

    @GetMapping("/orphan-images")
    public ApiResponse<OrphanImageScanDto> scanOrphanImages() throws Exception {
        return ApiResponse.success(uploadCleanupService.scanOrphanImages());
    }

    @PostMapping("/orphan-images/cleanup")
    public ApiResponse<OrphanImageCleanupDto> cleanupOrphanImages() throws Exception {
        OrphanImageCleanupDto result = uploadCleanupService.cleanupOrphanImages();
        String message = result.getDeletedCount() > 0
                ? "已删除 " + result.getDeletedCount() + " 个孤儿图片"
                : "没有可清理的孤儿图片";
        return ApiResponse.success(result, message);
    }

    @PostMapping("/orphan-tags/cleanup")
    public ApiResponse<Map<String, Integer>> cleanupOrphanTags() {
        int count = metaService.cleanupOrphanTags();
        return ApiResponse.success(Map.of("count", count), count > 0 ? "清理完成" : "没有可清理的标签");
    }
}
