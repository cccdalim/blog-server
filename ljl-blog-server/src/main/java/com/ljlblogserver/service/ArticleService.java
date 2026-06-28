package com.ljlblogserver.service;

import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.ArticleDto;
import com.ljlblogserver.dto.ArticleSaveRequest;
import com.ljlblogserver.dto.DocNavDto;
import com.ljlblogserver.dto.NavItemDto;
import com.ljlblogserver.entity.Article;
import com.ljlblogserver.entity.Category;
import com.ljlblogserver.entity.Tag;
import com.ljlblogserver.mapper.ArticleMapper;
import com.ljlblogserver.mapper.CategoryMapper;
import com.ljlblogserver.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final FileStorageService fileStorageService;
    private final ManagedUploadCleanupService uploadCleanupService;

    public PageResult<ArticleDto> list(ArticleType type, String categorySlug, String tag, String keyword,
                                       int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<Article> articles = articleMapper.findPage(
                type.getValue(), categorySlug, tag, keyword, offset, pageSize);
        long total = articleMapper.count(type.getValue(), categorySlug, tag, keyword);
        List<ArticleDto> list = articles.stream().map(a -> toDto(a, false)).toList();
        return new PageResult<>(list, total, page, pageSize);
    }

    public List<ArticleDto> latest(ArticleType type, int limit) {
        return articleMapper.findLatest(type.getValue(), limit).stream().map(a -> toDto(a, false)).toList();
    }

    public ArticleDto getBySlug(ArticleType type, String slug, boolean includeContent) {
        Article article = articleMapper.findBySlug(slug, type.getValue());
        if (article == null) {
            return null;
        }
        article.setTags(articleMapper.findTagsByArticleId(article.getId()));
        return toDto(article, includeContent);
    }

    public DocNavDto getNav(ArticleType type, String slug) {
        List<Article> sorted = articleMapper.findAllSorted(type.getValue());
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getSlug().equals(slug)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return new DocNavDto(null, null);
        }
        NavItemDto prev = index > 0
                ? new NavItemDto(sorted.get(index - 1).getSlug(), sorted.get(index - 1).getTitle())
                : null;
        NavItemDto next = index < sorted.size() - 1
                ? new NavItemDto(sorted.get(index + 1).getSlug(), sorted.get(index + 1).getTitle())
                : null;
        return new DocNavDto(prev, next);
    }

    @Transactional
    public ArticleDto create(ArticleType type, ArticleSaveRequest request) {
        if (articleMapper.findBySlug(request.getSlug(), type.getValue()) != null) {
            throw BusinessException.conflict("slug 已存在");
        }
        Category category = requireCategory(request.getCategorySlug());
        String contentPath = fileStorageService.saveMarkdown(type.getContentDir(), request.getSlug(), request.getContent());
        Article article = buildArticle(type, request, category, contentPath);
        articleMapper.insert(article);
        saveTags(article.getId(), request.getTags());
        article.setTags(request.getTags() != null ? request.getTags() : List.of());
        return toDto(article, true);
    }

    @Transactional
    public ArticleDto update(ArticleType type, String slug, ArticleSaveRequest request) {
        Article existing = articleMapper.findBySlug(slug, type.getValue());
        if (existing == null) {
            throw BusinessException.notFound("文章不存在");
        }
        if (!existing.getSlug().equals(request.getSlug())
                && articleMapper.findBySlug(request.getSlug(), type.getValue()) != null) {
            throw BusinessException.conflict("slug 已存在");
        }

        String oldMarkdown = fileStorageService.readMarkdown(existing.getContentPath());
        String oldCoverPublicUrl = fileStorageService.toPublicUrl(existing.getCoverPath());
        uploadCleanupService.cleanupRemovedAssets(
                oldCoverPublicUrl,
                oldMarkdown,
                request.getCover(),
                request.getContent(),
                existing.getId());

        fileStorageService.deleteIfExists(existing.getContentPath());
        if (!StringUtils.hasText(request.getSlug())) {
            request.setSlug(slug);
        }

        String contentPath = fileStorageService.saveMarkdown(type.getContentDir(), request.getSlug(), request.getContent());
        Category category = requireCategory(request.getCategorySlug());

        if (!existing.getSlug().equals(request.getSlug())) {
            articleMapper.deleteTagsByArticleId(existing.getId());
            articleMapper.deleteBySlug(slug, type.getValue());
            Article article = buildArticle(type, request, category, contentPath);
            articleMapper.insert(article);
            saveTags(article.getId(), request.getTags());
            cleanupOrphanTags();
            article.setTags(request.getTags() != null ? request.getTags() : List.of());
            return toDto(article, true);
        }

        existing.setTitle(request.getTitle());
        existing.setSummary(request.getSummary());
        existing.setCoverPath(fileStorageService.normalizeStoredPath(request.getCover()));
        existing.setContentPath(contentPath);
        existing.setCategoryId(category.getId());
        existing.setCategoryName(category.getName());
        existing.setCategorySlug(category.getSlug());
        existing.setPublishDate(request.getPublishDate());
        existing.setReadTime(request.getReadTime());
        existing.setFeatured(Boolean.TRUE.equals(request.getFeatured()));
        existing.setArticleType(type.getValue());
        int rows = articleMapper.update(existing);
        if (rows == 0) {
            throw new BusinessException(500, "文章更新失败，请刷新后重试");
        }

        articleMapper.deleteTagsByArticleId(existing.getId());
        saveTags(existing.getId(), request.getTags());
        cleanupOrphanTags();
        existing.setTags(request.getTags() != null ? request.getTags() : List.of());
        return toDto(existing, true);
    }

    @Transactional
    public void delete(ArticleType type, String slug) {
        Article existing = articleMapper.findBySlug(slug, type.getValue());
        if (existing == null) {
            throw BusinessException.notFound("文章不存在");
        }
        String markdown = fileStorageService.readMarkdown(existing.getContentPath());
        String coverPublicUrl = fileStorageService.toPublicUrl(existing.getCoverPath());

        fileStorageService.deleteIfExists(existing.getContentPath());
        articleMapper.deleteTagsByArticleId(existing.getId());
        articleMapper.deleteBySlug(slug, type.getValue());
        cleanupOrphanTags();

        uploadCleanupService.deleteArticleAssets(coverPublicUrl, markdown);
    }

    @Transactional
    public void deleteAll(ArticleType type) {
        List<Article> articles = articleMapper.findAllSorted(type.getValue());
        for (Article article : articles) {
            delete(type, article.getSlug());
        }
    }

    private void cleanupOrphanTags() {
        tagMapper.deleteOrphans();
    }

    private Category requireCategory(String categorySlug) {
        Category category = categoryMapper.findBySlug(categorySlug);
        if (category == null) {
            throw BusinessException.badRequest("分类不存在: " + categorySlug);
        }
        return category;
    }

    private Article buildArticle(ArticleType type, ArticleSaveRequest request, Category category, String contentPath) {
        Article article = new Article();
        article.setArticleType(type.getValue());
        article.setSlug(request.getSlug());
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setCoverPath(fileStorageService.normalizeStoredPath(request.getCover()));
        article.setContentPath(contentPath);
        article.setCategoryId(category.getId());
        article.setCategoryName(category.getName());
        article.setCategorySlug(category.getSlug());
        article.setPublishDate(request.getPublishDate());
        article.setReadTime(request.getReadTime());
        article.setFeatured(Boolean.TRUE.equals(request.getFeatured()));
        return article;
    }

    private void saveTags(Long articleId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (String tagName : tags) {
            if (!StringUtils.hasText(tagName)) {
                continue;
            }
            String slug = slugify(tagName);
            Tag tag = tagMapper.findBySlug(slug);
            if (tag == null) {
                tag = tagMapper.findByName(tagName.trim());
            }
            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName.trim());
                tag.setSlug(StringUtils.hasText(slug) ? slug : tagName.trim());
                tagMapper.insert(tag);
                if (tag.getId() == null) {
                    tag = tagMapper.findBySlug(tag.getSlug());
                }
            }
            if (tag == null || tag.getId() == null) {
                throw BusinessException.badRequest("标签保存失败: " + tagName);
            }
            articleMapper.insertArticleTag(articleId, tag.getId());
        }
    }

    private ArticleDto toDto(Article article, boolean includeContent) {
        ArticleDto dto = new ArticleDto();
        dto.setId(String.valueOf(article.getId()));
        dto.setSlug(article.getSlug());
        dto.setTitle(article.getTitle());
        dto.setSummary(article.getSummary());
        dto.setCover(fileStorageService.toPublicUrl(article.getCoverPath()));
        dto.setCategory(article.getCategoryName());
        dto.setCategorySlug(article.getCategorySlug());
        List<String> tags = article.getTags();
        if (tags == null || tags.isEmpty()) {
            tags = article.getId() != null ? articleMapper.findTagsByArticleId(article.getId()) : List.of();
        }
        dto.setTags(tags);
        dto.setPublishDate(article.getPublishDate() != null ? article.getPublishDate().toString() : null);
        dto.setReadTime(article.getReadTime());
        dto.setFeatured(article.getFeatured());
        if (includeContent) {
            dto.setContent(fileStorageService.readMarkdown(article.getContentPath()));
        }
        return dto;
    }

    private String slugify(String input) {
        return input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }
}
