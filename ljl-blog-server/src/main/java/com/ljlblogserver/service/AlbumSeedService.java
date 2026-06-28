package com.ljlblogserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.dto.PhotoSaveRequest;
import com.ljlblogserver.mapper.PhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AlbumSeedService {

    private final PhotoMapper photoMapper;
    private final PhotoService photoService;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importSeed(boolean replace) throws Exception {
        if (replace) {
            photoMapper.deleteAll();
        } else if (photoMapper.count(null, null, null) > 0) {
            return 0;
        }

        ClassPathResource resource = new ClassPathResource("seed/album.json");
        try (InputStream in = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            JsonNode list = root.get("list");
            if (list == null || !list.isArray()) {
                return 0;
            }
            int count = 0;
            for (JsonNode node : list) {
                PhotoSaveRequest request = new PhotoSaveRequest();
                request.setUrl(node.path("url").asText());
                request.setTitle(node.path("title").asText());
                request.setCity(node.path("city").asText());
                request.setCountry(node.path("country").asText());
                request.setCategory(node.path("category").asText());
                request.setDate(LocalDate.parse(node.path("date").asText()));
                if (node.hasNonNull("width")) {
                    request.setWidth(node.path("width").asInt());
                }
                if (node.hasNonNull("height")) {
                    request.setHeight(node.path("height").asInt());
                }
                photoService.create(request);
                count++;
            }
            return count;
        }
    }
}
