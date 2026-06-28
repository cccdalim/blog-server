package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.dto.UploadResponse;
import com.ljlblogserver.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    public ApiResponse<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.saveImage(file);
        return ApiResponse.success(new UploadResponse(url, url), "上传成功");
    }
}
