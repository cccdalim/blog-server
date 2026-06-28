package com.ljlblogserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.config.StorageProperties;
import com.ljlblogserver.dto.AboutDto.AboutDataDto;
import com.ljlblogserver.dto.AboutDto.AboutPageDto;
import com.ljlblogserver.dto.AboutDto.SkillDto;
import com.ljlblogserver.dto.AboutDto.SkillsFileDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AboutService {

    private static final String ABOUT_SEED = "seed/about-page.json";
    private static final String SKILLS_SEED = "seed/skills.json";

    private final ObjectMapper objectMapper;
    private final StorageProperties storageProperties;

    private Path siteDir;
    private Path aboutPagePath;
    private Path skillsPath;

    @PostConstruct
    void init() throws IOException {
        Path root = Paths.get(storageProperties.getRoot()).toAbsolutePath().normalize();
        siteDir = root.resolve("content/site");
        aboutPagePath = siteDir.resolve("about-page.json");
        skillsPath = siteDir.resolve("skills.json");
        Files.createDirectories(siteDir);
        seedIfMissing(aboutPagePath, ABOUT_SEED);
        seedIfMissing(skillsPath, SKILLS_SEED);
    }

    public AboutDataDto getAbout() {
        AboutPageDto page = getAboutPage();
        AboutDataDto data = new AboutDataDto();
        data.setProfile(page.getProfile());
        data.setSocial(page.getSocial());
        return data;
    }

    public AboutPageDto getAboutPage() {
        return readJson(aboutPagePath, AboutPageDto.class);
    }

    public List<SkillDto> getSkills() {
        SkillsFileDto file = readJson(skillsPath, SkillsFileDto.class);
        return file.getSkills();
    }

    public AboutPageDto saveAboutPage(AboutPageDto page) {
        if (page == null || page.getProfile() == null
                || page.getProfile().getName() == null || page.getProfile().getName().isBlank()) {
            throw BusinessException.badRequest("姓名不能为空");
        }
        writeJson(aboutPagePath, page);
        return page;
    }

    public List<SkillDto> saveSkills(List<SkillDto> skills) {
        SkillsFileDto file = new SkillsFileDto();
        file.setSkills(skills != null ? skills : List.of());
        writeJson(skillsPath, file);
        return file.getSkills();
    }

    public int importSeed(boolean replace) throws IOException {
        if (replace) {
            copyFromClasspath(aboutPagePath, ABOUT_SEED);
            copyFromClasspath(skillsPath, SKILLS_SEED);
            return 2;
        }
        int count = 0;
        if (!Files.exists(aboutPagePath)) {
            copyFromClasspath(aboutPagePath, ABOUT_SEED);
            count++;
        }
        if (!Files.exists(skillsPath)) {
            copyFromClasspath(skillsPath, SKILLS_SEED);
            count++;
        }
        return count;
    }

    private void copyFromClasspath(Path target, String classpathResource) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathResource);
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void seedIfMissing(Path target, String classpathResource) throws IOException {
        if (Files.exists(target)) {
            return;
        }
        copyFromClasspath(target, classpathResource);
    }

    private void writeJson(Path path, Object data) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
        } catch (IOException ex) {
            throw new BusinessException(500, "保存站点配置失败: " + ex.getMessage());
        }
    }

    private <T> T readJson(Path path, Class<T> type) {
        try {
            return objectMapper.readValue(path.toFile(), type);
        } catch (IOException ex) {
            throw new BusinessException(500, "读取站点配置失败: " + ex.getMessage());
        }
    }
}
