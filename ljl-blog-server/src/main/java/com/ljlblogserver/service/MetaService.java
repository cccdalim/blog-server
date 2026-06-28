package com.ljlblogserver.service;

import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.dto.CategoryDto;
import com.ljlblogserver.dto.MetaSaveRequest;
import com.ljlblogserver.dto.TagDto;
import com.ljlblogserver.entity.Category;
import com.ljlblogserver.entity.Tag;
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
public class MetaService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public List<CategoryDto> categories() {
        return categoryMapper.findAllWithCount().stream()
                .map(this::toCategoryDto)
                .toList();
    }

    public List<TagDto> tags() {
        return tagMapper.findAllWithCount().stream()
                .map(this::toTagDto)
                .toList();
    }

    @Transactional
    public CategoryDto createCategory(MetaSaveRequest request) {
        String slug = normalizeSlug(request.getSlug(), request.getName());
        if (categoryMapper.findBySlug(slug) != null) {
            throw BusinessException.conflict("分类 slug 已存在");
        }
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(slug);
        categoryMapper.insert(category);
        return toCategoryDto(category, 0);
    }

    @Transactional
    public CategoryDto updateCategory(String id, MetaSaveRequest request) {
        Category existing = requireCategory(id);
        String slug = normalizeSlug(request.getSlug(), request.getName());
        Category conflict = categoryMapper.findBySlugExclude(slug, existing.getId());
        if (conflict != null) {
            throw BusinessException.conflict("分类 slug 已存在");
        }
        existing.setName(request.getName().trim());
        existing.setSlug(slug);
        categoryMapper.update(existing);
        long count = categoryMapper.countArticlesByCategoryId(existing.getId());
        return toCategoryDto(existing, (int) count);
    }

    @Transactional
    public void deleteCategory(String id) {
        Category existing = requireCategory(id);
        long count = categoryMapper.countArticlesByCategoryId(existing.getId());
        if (count > 0) {
            throw BusinessException.conflict("仍有 " + count + " 篇文章使用该分类，无法删除");
        }
        categoryMapper.deleteById(existing.getId());
    }

    @Transactional
    public TagDto createTag(MetaSaveRequest request) {
        String slug = normalizeSlug(request.getSlug(), request.getName());
        if (tagMapper.findBySlug(slug) != null) {
            throw BusinessException.conflict("标签 slug 已存在");
        }
        Tag tag = new Tag();
        tag.setName(request.getName().trim());
        tag.setSlug(slug);
        tagMapper.insert(tag);
        return toTagDto(tag, 0);
    }

    @Transactional
    public TagDto updateTag(String id, MetaSaveRequest request) {
        Tag existing = requireTag(id);
        String slug = normalizeSlug(request.getSlug(), request.getName());
        Tag conflict = tagMapper.findBySlugExclude(slug, existing.getId());
        if (conflict != null) {
            throw BusinessException.conflict("标签 slug 已存在");
        }
        existing.setName(request.getName().trim());
        existing.setSlug(slug);
        tagMapper.update(existing);
        long count = tagMapper.countArticlesByTagId(existing.getId());
        return toTagDto(existing, (int) count);
    }

    @Transactional
    public void deleteTag(String id) {
        Tag existing = requireTag(id);
        long count = tagMapper.countArticlesByTagId(existing.getId());
        if (count > 0) {
            throw BusinessException.conflict("仍有 " + count + " 篇文章使用该标签，无法删除");
        }
        tagMapper.deleteById(existing.getId());
    }

    @Transactional
    public int cleanupOrphanTags() {
        return tagMapper.deleteOrphans();
    }

    private Category requireCategory(String id) {
        Category category = findCategoryOrNull(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        return category;
    }

    private Tag requireTag(String id) {
        Tag tag = findTagOrNull(id);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }
        return tag;
    }

    private Category findCategoryOrNull(String id) {
        try {
            return categoryMapper.findById(Long.valueOf(id));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Tag findTagOrNull(String id) {
        try {
            return tagMapper.findById(Long.valueOf(id));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private CategoryDto toCategoryDto(Category category) {
        return toCategoryDto(category, category.getCount() != null ? category.getCount() : 0);
    }

    private CategoryDto toCategoryDto(Category category, int count) {
        return new CategoryDto(String.valueOf(category.getId()), category.getName(), category.getSlug(), count);
    }

    private TagDto toTagDto(Tag tag) {
        return toTagDto(tag, tag.getCount() != null ? tag.getCount() : 0);
    }

    private TagDto toTagDto(Tag tag, int count) {
        return new TagDto(String.valueOf(tag.getId()), tag.getName(), tag.getSlug(), count);
    }

    private String normalizeSlug(String slug, String name) {
        String value = StringUtils.hasText(slug) ? slug : name;
        return slugify(value);
    }

    private String slugify(String input) {
        if (!StringUtils.hasText(input)) {
            throw BusinessException.badRequest("slug 不能为空");
        }
        return input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }
}
