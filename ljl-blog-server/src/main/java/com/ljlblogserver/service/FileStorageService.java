package com.ljlblogserver.service;

import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final StorageProperties storageProperties;

    private Path rootPath;
    private Path contentPath;
    private Path uploadPath;

    @PostConstruct
    void init() throws IOException {
        rootPath = Paths.get(storageProperties.getRoot()).toAbsolutePath().normalize();
        contentPath = rootPath.resolve(storageProperties.getContentDir());
        uploadPath = rootPath.resolve(storageProperties.getUploadDir());
        Files.createDirectories(contentPath);
        Files.createDirectories(uploadPath);
    }

    public String saveMarkdown(String slug, String content) {
        return saveMarkdown(storageProperties.getContentDir(), slug, content);
    }

    public String saveMarkdown(String contentDir, String slug, String content) {
        try {
            Path dir = rootPath.resolve(contentDir);
            Files.createDirectories(dir);
            Path file = dir.resolve(slug + ".md");
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return toRelativePath(file);
        } catch (IOException ex) {
            throw new BusinessException(500, "保存 Markdown 失败: " + ex.getMessage());
        }
    }

    public String readMarkdown(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return "";
        }
        try {
            Path file = resolveRelativePath(relativePath);
            if (!Files.exists(file)) {
                return "";
            }
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException(500, "读取 Markdown 失败: " + ex.getMessage());
        }
    }

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("上传文件不能为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_IMAGE_EXT.contains(ext.toLowerCase())) {
            throw BusinessException.badRequest("仅支持 jpg/jpeg/png/gif/webp 格式");
        }
        try {
            String filename = UUID.randomUUID() + "." + ext.toLowerCase();
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);
            String relative = toRelativePath(target);
            return toPublicUrl(relative);
        } catch (IOException ex) {
            throw new BusinessException(500, "保存图片失败: " + ex.getMessage());
        }
    }

    public String normalizeStoredPath(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith(storageProperties.getPublicUrlPrefix())) {
            return toRelativeFromPublicUrl(value);
        }
        return value.replace("\\", "/");
    }

    public String toPublicUrl(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        String normalized = relativePath.replace("\\", "/");
        if (normalized.startsWith(storageProperties.getPublicUrlPrefix())) {
            return normalized;
        }
        String fileName = Paths.get(normalized).getFileName().toString();
        return storageProperties.getPublicUrlPrefix() + "/images/" + fileName;
    }

    public void deleteIfExists(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return;
        }
        try {
            Path file = resolveRelativePath(relativePath);
            Files.deleteIfExists(file);
        } catch (IOException | BusinessException ignored) {
            // 忽略删除失败
        }
    }

    public Path getUploadRootPath() {
        return uploadPath;
    }

    private Path resolveRelativePath(String relativePath) {
        Path path = rootPath.resolve(relativePath.replace("\\", "/")).normalize();
        if (!path.startsWith(rootPath)) {
            throw BusinessException.badRequest("非法文件路径");
        }
        return path;
    }

    private String toRelativePath(Path absolutePath) {
        return rootPath.relativize(absolutePath.toAbsolutePath().normalize()).toString().replace("\\", "/");
    }

    private String toRelativeFromPublicUrl(String publicUrl) {
        String prefix = storageProperties.getPublicUrlPrefix() + "/images/";
        if (publicUrl.startsWith(prefix)) {
            return storageProperties.getUploadDir() + "/" + publicUrl.substring(prefix.length());
        }
        return publicUrl;
    }
}
