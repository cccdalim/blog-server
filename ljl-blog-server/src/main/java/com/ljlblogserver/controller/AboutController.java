package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.dto.AboutDto.AboutDataDto;
import com.ljlblogserver.dto.AboutDto.AboutPageDto;
import com.ljlblogserver.dto.AboutDto.LearningPathItemDto;
import com.ljlblogserver.dto.AboutDto.SkillDto;
import com.ljlblogserver.dto.AboutDto.SkillsFileDto;
import com.ljlblogserver.dto.AboutDto.TimelineItemDto;
import com.ljlblogserver.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping
    public ApiResponse<AboutDataDto> about() {
        return ApiResponse.success(aboutService.getAbout());
    }

    @GetMapping("/page")
    public ApiResponse<AboutPageDto> aboutPage() {
        return ApiResponse.success(aboutService.getAboutPage());
    }

    @GetMapping("/skills")
    public ApiResponse<List<SkillDto>> skills() {
        return ApiResponse.success(aboutService.getSkills());
    }

    @GetMapping("/timeline")
    public ApiResponse<List<TimelineItemDto>> timeline() {
        return ApiResponse.success(aboutService.getAboutPage().getTimeline());
    }

    @GetMapping("/learning-path")
    public ApiResponse<List<LearningPathItemDto>> learningPath() {
        return ApiResponse.success(aboutService.getAboutPage().getLearningPath());
    }

    @PutMapping("/page")
    public ApiResponse<AboutPageDto> updatePage(@RequestBody AboutPageDto request) {
        return ApiResponse.success(aboutService.saveAboutPage(request), "保存成功");
    }

    @PutMapping("/skills")
    public ApiResponse<List<SkillDto>> updateSkills(@RequestBody SkillsFileDto request) {
        List<SkillDto> skills = request != null ? request.getSkills() : List.of();
        return ApiResponse.success(aboutService.saveSkills(skills), "保存成功");
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Integer>> seed(@RequestParam(defaultValue = "false") boolean replace)
            throws Exception {
        int count = aboutService.importSeed(replace);
        return ApiResponse.success(Map.of("count", count), count > 0 ? "导入成功" : "已有数据，未导入");
    }
}
