package com.ljlblogserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.entity.Project;
import com.ljlblogserver.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectSeedService {

    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importSeed(boolean replace) throws Exception {
        if (replace) {
            projectMapper.deleteAll();
        } else if (projectMapper.countAll() > 0) {
            return 0;
        }

        ClassPathResource resource = new ClassPathResource("seed/projects.json");
        try (InputStream in = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            JsonNode list = root.get("list");
            if (list == null || !list.isArray()) {
                return 0;
            }
            int count = 0;
            for (JsonNode node : list) {
                Project project = new Project();
                project.setName(node.path("name").asText());
                project.setDescription(node.path("description").asText());
                project.setSummary(node.path("summary").asText());
                project.setCover(textOrNull(node, "cover"));
                project.setGitUrl(textOrNull(node, "gitUrl"));
                project.setDemoUrl(textOrNull(node, "demoUrl"));
                project.setFeatured(node.path("featured").asBoolean(false));
                project.setStartDate(LocalDate.parse(node.path("startDate").asText()));
                if (!node.path("endDate").isNull()) {
                    project.setEndDate(LocalDate.parse(node.path("endDate").asText()));
                }
                project.setTechStackJson(writeJson(node.get("techStack")));
                project.setDevProcessJson(writeJson(node.get("devProcess")));
                project.setScreenshotsJson(writeJson(node.get("screenshots")));
                project.setTimelineJson(writeJson(node.get("timeline")));
                projectMapper.insert(project);
                count++;
            }
            return count;
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String writeJson(JsonNode node) throws Exception {
        if (node == null || node.isNull()) {
            return "[]";
        }
        return objectMapper.writeValueAsString(node);
    }
}
