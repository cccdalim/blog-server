package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.ArticleDto;
import com.ljlblogserver.dto.ArticleSaveRequest;
import com.ljlblogserver.dto.DocNavDto;
import com.ljlblogserver.service.ArticleService;
import com.ljlblogserver.service.RecipeSeedService;
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
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final ArticleService articleService;
    private final RecipeSeedService recipeSeedService;

    @GetMapping("/list")
    public ApiResponse<PageResult<ArticleDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(
                articleService.list(ArticleType.RECIPE, category, tag, keyword, page, pageSize));
    }

    @GetMapping("/latest")
    public ApiResponse<List<ArticleDto>> latest(@RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.success(articleService.latest(ArticleType.RECIPE, limit));
    }

    @GetMapping("/{slug}")
    public ApiResponse<ArticleDto> detail(@PathVariable String slug) {
        return ApiResponse.success(articleService.getBySlug(ArticleType.RECIPE, slug, true));
    }

    @GetMapping("/{slug}/nav")
    public ApiResponse<DocNavDto> nav(@PathVariable String slug) {
        return ApiResponse.success(articleService.getNav(ArticleType.RECIPE, slug));
    }

    @PostMapping
    public ApiResponse<ArticleDto> create(@Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.success(articleService.create(ArticleType.RECIPE, request), "创建成功");
    }

    @PutMapping("/{slug}")
    public ApiResponse<ArticleDto> update(@PathVariable String slug, @Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.success(articleService.update(ArticleType.RECIPE, slug, request), "更新成功");
    }

    @DeleteMapping("/{slug}")
    public ApiResponse<Void> delete(@PathVariable String slug) {
        articleService.delete(ArticleType.RECIPE, slug);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Integer>> seed(@RequestParam(defaultValue = "false") boolean replace)
            throws Exception {
        int count = recipeSeedService.importSeed(replace);
        return ApiResponse.success(Map.of("count", count), count > 0 ? "导入成功" : "已有数据，未导入");
    }
}
