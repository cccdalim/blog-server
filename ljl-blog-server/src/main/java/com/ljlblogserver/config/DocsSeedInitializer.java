package com.ljlblogserver.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.dto.ArticleSaveRequest;
import com.ljlblogserver.mapper.ArticleMapper;
import com.ljlblogserver.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocsSeedInitializer implements CommandLineRunner {

    private final ArticleMapper articleMapper;
    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (articleMapper.count(ArticleType.DOC.getValue(), null, null, null) > 0) {
            return;
        }

        ClassPathResource listResource = new ClassPathResource("seed/docs.json");
        ClassPathResource contentResource = new ClassPathResource("seed/docs-content.json");

        JsonNode listRoot;
        JsonNode contentRoot;
        try (var listIn = listResource.getInputStream(); var contentIn = contentResource.getInputStream()) {
            listRoot = objectMapper.readTree(listIn);
            contentRoot = objectMapper.readTree(contentIn);
        }

        JsonNode list = listRoot.get("list");
        if (list == null || !list.isArray()) {
            return;
        }

        int imported = 0;
        for (JsonNode node : list) {
            String slug = node.path("slug").asText();
            String content = contentRoot.path(slug).asText("");
            if (content.isBlank()) {
                log.warn("跳过文档 {}：未找到正文", slug);
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

            articleService.create(ArticleType.DOC, request);
            imported++;
        }

        log.info("已导入 {} 条示例文档数据", imported);
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
