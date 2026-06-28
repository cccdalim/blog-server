package com.ljlblogserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.dto.ArticleSaveRequest;
import com.ljlblogserver.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogSeedService {

    private final ArticleMapper articleMapper;
    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importSeed(boolean replace) throws Exception {
        if (replace) {
            articleService.deleteAll(ArticleType.BLOG);
        } else if (articleMapper.count(ArticleType.BLOG.getValue(), null, null, null) > 0) {
            return 0;
        }

        ClassPathResource listResource = new ClassPathResource("seed/blog.json");
        ClassPathResource contentResource = new ClassPathResource("seed/blog-content.json");

        JsonNode listRoot;
        JsonNode contentRoot;
        try (var listIn = listResource.getInputStream(); var contentIn = contentResource.getInputStream()) {
            listRoot = objectMapper.readTree(listIn);
            contentRoot = objectMapper.readTree(contentIn);
        }

        JsonNode list = listRoot.get("list");
        if (list == null || !list.isArray()) {
            return 0;
        }

        int imported = 0;
        for (JsonNode node : list) {
            String slug = node.path("slug").asText();
            String content = contentRoot.path(slug).asText("");
            if (content.isBlank()) {
                continue;
            }

            ArticleSaveRequest request = new ArticleSaveRequest();
            request.setSlug(slug);
            request.setTitle(node.path("title").asText());
            request.setSummary(node.path("summary").asText());
            request.setContent(content);
            request.setCover(textOrNull(node, "cover"));
            request.setCategorySlug(node.path("categorySlug").asText());
            request.setPublishDate(LocalDate.parse(node.path("publishDate").asText()));
            request.setReadTime(node.path("readTime").asInt(10));
            request.setFeatured(node.path("featured").asBoolean(false));
            request.setTags(readTags(node.get("tags")));

            articleService.create(ArticleType.BLOG, request);
            imported++;
        }
        return imported;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private List<String> readTags(JsonNode tagsNode) {
        List<String> tags = new ArrayList<>();
        if (tagsNode != null && tagsNode.isArray()) {
            tagsNode.forEach(tag -> tags.add(tag.asText()));
        }
        return tags;
    }
}
