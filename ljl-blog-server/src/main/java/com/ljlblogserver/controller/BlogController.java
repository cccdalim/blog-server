package com.ljlblogserver.controller;

import com.ljlblogserver.common.ApiResponse;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.ArticleDto;
import com.ljlblogserver.dto.ArticleSaveRequest;
import com.ljlblogserver.dto.DocNavDto;
import com.ljlblogserver.service.ArticleService;
import com.ljlblogserver.service.BlogSeedService;
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
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final ArticleService articleService;
    private final BlogSeedService blogSeedService;

    @GetMapping("/list")
    public ApiResponse<PageResult<ArticleDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(
                articleService.list(ArticleType.BLOG, category, tag, keyword, page, pageSize));
    }

    @GetMapping("/latest")
    public ApiResponse<List<ArticleDto>> latest(@RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.success(articleService.latest(ArticleType.BLOG, limit));
    }

    @GetMapping("/{slug}")
    public ApiResponse<ArticleDto> detail(@PathVariable String slug) {
        return ApiResponse.success(articleService.getBySlug(ArticleType.BLOG, slug, true));
    }

    @GetMapping("/{slug}/nav")
    public ApiResponse<DocNavDto> nav(@PathVariable String slug) {
        return ApiResponse.success(articleService.getNav(ArticleType.BLOG, slug));
    }

    @PostMapping
    public ApiResponse<ArticleDto> create(@Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.success(articleService.create(ArticleType.BLOG, request), "创建成功");
    }

    @PutMapping("/{slug}")
    public ApiResponse<ArticleDto> update(@PathVariable String slug, @Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.success(articleService.update(ArticleType.BLOG, slug, request), "更新成功");
    }

    @DeleteMapping("/{slug}")
    public ApiResponse<Void> delete(@PathVariable String slug) {
        articleService.delete(ArticleType.BLOG, slug);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Integer>> seed(@RequestParam(defaultValue = "false") boolean replace)
            throws Exception {
        int count = blogSeedService.importSeed(replace);
        return ApiResponse.success(Map.of("count", count), count > 0 ? "导入成功" : "已有数据，未导入");
    }
}
