package com.ljlblogserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.config.StorageProperties;
import com.ljlblogserver.dto.AboutDto.AboutPageDto;
import com.ljlblogserver.dto.OrphanImageCleanupDto;
import com.ljlblogserver.dto.OrphanImageScanDto;
import com.ljlblogserver.dto.OrphanImageScanDto.OrphanImageItemDto;
import com.ljlblogserver.entity.Article;
import com.ljlblogserver.entity.Photo;
import com.ljlblogserver.entity.Project;
import com.ljlblogserver.mapper.ArticleMapper;
import com.ljlblogserver.mapper.PhotoMapper;
import com.ljlblogserver.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理 uploads/images 下由本站上传的资源：提取引用、删除前检查是否仍被占用。
 */
@Service
@RequiredArgsConstructor
public class ManagedUploadCleanupService {

    private static final Pattern MARKDOWN_IMAGE = Pattern.compile("!\\[[^\\]]*]\\(([^)]+)\\)");
    private static final Pattern HTML_IMAGE = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private final FileStorageService fileStorageService;
    private final StorageProperties storageProperties;
    private final ArticleMapper articleMapper;
    private final PhotoMapper photoMapper;
    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;
    private final AboutService aboutService;

    /** 从项目封面 + 截图 URL 列表收集本站托管图片的相对路径（去重） */
    public Set<String> collectProjectManagedPaths(String cover, Collection<String> screenshotUrls) {
        Set<String> paths = new LinkedHashSet<>();
        addManagedPath(paths, cover);
        if (screenshotUrls != null) {
            screenshotUrls.forEach(url -> addManagedPath(paths, url));
        }
        return paths;
    }

    /** 从封面 + Markdown 正文中收集本站托管图片的相对路径（去重） */
    public Set<String> collectManagedPaths(String cover, String markdown) {
        Set<String> paths = new LinkedHashSet<>();
        addManagedPath(paths, cover);
        if (StringUtils.hasText(markdown)) {
            extractUrls(markdown).forEach(url -> addManagedPath(paths, url));
        }
        return paths;
    }

    /** 旧有但新内容中已移除的图片 */
    public Set<String> diffRemoved(Set<String> oldPaths, Set<String> newPaths) {
        Set<String> removed = new LinkedHashSet<>(oldPaths);
        removed.removeAll(newPaths);
        return removed;
    }

    /** 删除一批候选图片（仅删除未被其他文章/相册引用的） */
    public void deleteIfUnreferenced(Collection<String> relativePaths, Long excludeArticleId) {
        deleteIfUnreferenced(relativePaths, excludeArticleId, null, null);
    }

    /** 删除一批候选图片（仅删除未被其他内容引用的） */
    public void deleteIfUnreferenced(Collection<String> relativePaths, Long excludeArticleId,
                                     String excludeNewCover, String excludeNewMarkdown) {
        for (String path : relativePaths) {
            deleteOneIfUnreferenced(path, excludeArticleId, excludeNewCover, excludeNewMarkdown, null, null, null);
        }
    }

    /** 删除文章时：清理该文所有托管图片（封面 + 正文） */
    public void deleteArticleAssets(String coverPublicUrl, String markdown) {
        Set<String> paths = collectManagedPaths(coverPublicUrl, markdown);
        deleteIfUnreferenced(paths, null);
    }

    /** 编辑文章时：清理被替换/移除的图片 */
    public void cleanupRemovedAssets(String oldCover, String oldMarkdown, String newCover, String newMarkdown,
                                     Long excludeArticleId) {
        Set<String> oldPaths = collectManagedPaths(oldCover, oldMarkdown);
        Set<String> newPaths = collectManagedPaths(newCover, newMarkdown);
        deleteIfUnreferenced(diffRemoved(oldPaths, newPaths), excludeArticleId, newCover, newMarkdown);
    }

    /** 删除项目时：清理该项目所有托管图片（封面 + 截图） */
    public void deleteProjectAssets(String cover, Collection<String> screenshotUrls) {
        Set<String> paths = collectProjectManagedPaths(cover, screenshotUrls);
        deleteIfUnreferencedByProject(paths, null, null, null);
    }

    /** 编辑项目时：清理被替换/移除的封面与截图 */
    public void cleanupRemovedProjectAssets(String oldCover, Collection<String> oldScreenshots,
                                            String newCover, Collection<String> newScreenshots,
                                            Long excludeProjectId) {
        Set<String> oldPaths = collectProjectManagedPaths(oldCover, oldScreenshots);
        Set<String> newPaths = collectProjectManagedPaths(newCover, newScreenshots);
        deleteIfUnreferencedByProject(
                diffRemoved(oldPaths, newPaths),
                excludeProjectId,
                newCover,
                newScreenshots);
    }

    /** 删除一批候选图片（项目编辑/删除场景） */
    public void deleteIfUnreferencedByProject(Collection<String> relativePaths, Long excludeProjectId,
                                              String excludeNewCover, Collection<String> excludeNewScreenshots) {
        for (String path : relativePaths) {
            deleteOneIfUnreferenced(path, null, null, null, excludeProjectId, excludeNewCover, excludeNewScreenshots);
        }
    }

    /** 扫描 uploads/images 中未被任何内容引用的孤儿图片 */
    public OrphanImageScanDto scanOrphanImages() throws IOException {
        Set<String> referenced = collectAllReferencedPaths();
        Path uploadRoot = fileStorageService.getUploadRootPath();
        OrphanImageScanDto result = new OrphanImageScanDto();
        List<OrphanImageItemDto> orphans = new ArrayList<>();

        if (!Files.isDirectory(uploadRoot)) {
            result.setReferencedCount(referenced.size());
            return result;
        }

        long orphanSize = 0;
        int total = 0;
        try (var stream = Files.list(uploadRoot)) {
            for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
                total++;
                String relative = normalizeRelativePath(
                        storageProperties.getUploadDir().replace("\\", "/") + "/" + file.getFileName());
                if (relative == null || referenced.contains(relative)) {
                    continue;
                }
                long size = Files.size(file);
                orphans.add(new OrphanImageItemDto(
                        relative,
                        fileStorageService.toPublicUrl(relative),
                        size));
                orphanSize += size;
            }
        }

        result.setTotalFiles(total);
        result.setReferencedCount(total - orphans.size());
        result.setOrphanCount(orphans.size());
        result.setOrphanSizeBytes(orphanSize);
        result.setOrphans(orphans);
        return result;
    }

    /** 删除所有孤儿图片 */
    public OrphanImageCleanupDto cleanupOrphanImages() throws IOException {
        OrphanImageScanDto scan = scanOrphanImages();
        OrphanImageCleanupDto result = new OrphanImageCleanupDto();
        long freed = 0;
        for (OrphanImageItemDto orphan : scan.getOrphans()) {
            fileStorageService.deleteIfExists(orphan.getPath());
            result.getDeleted().add(orphan.getPath());
            freed += orphan.getSizeBytes();
        }
        result.setDeletedCount(result.getDeleted().size());
        result.setFreedBytes(freed);
        return result;
    }

    /** 收集全站所有托管图片引用 */
    public Set<String> collectAllReferencedPaths() {
        Set<String> paths = new LinkedHashSet<>();

        for (Article article : articleMapper.findAllForAssetScan()) {
            addManagedPath(paths, fileStorageService.toPublicUrl(article.getCoverPath()));
            addManagedPath(paths, article.getCoverPath());
            String markdown = safeReadMarkdown(article);
            extractUrls(markdown).forEach(url -> addManagedPath(paths, url));
        }

        for (Project project : projectMapper.findAllSorted()) {
            addManagedPath(paths, project.getCover());
            readStringListJson(project.getScreenshotsJson()).forEach(url -> addManagedPath(paths, url));
        }

        for (Photo photo : photoMapper.findAll(null, null, null)) {
            addManagedPath(paths, fileStorageService.toPublicUrl(photo.getFilePath()));
            addManagedPath(paths, photo.getFilePath());
        }

        try {
            AboutPageDto aboutPage = aboutService.getAboutPage();
            if (aboutPage.getProfile() != null) {
                addManagedPath(paths, aboutPage.getProfile().getAvatar());
            }
        } catch (RuntimeException ignored) {
            // 关于页配置缺失时跳过
        }

        return paths;
    }

    private void deleteOneIfUnreferenced(String relativePath, Long excludeArticleId,
                                         String excludeNewArticleCover, String excludeNewArticleMarkdown,
                                         Long excludeProjectId, String excludeNewProjectCover,
                                         Collection<String> excludeNewProjectScreenshots) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        String normalized = normalizeRelativePath(relativePath);
        if (normalized == null) {
            return;
        }
        if (isReferenced(normalized, excludeArticleId, excludeNewArticleCover, excludeNewArticleMarkdown,
                excludeProjectId, excludeNewProjectCover, excludeNewProjectScreenshots)) {
            return;
        }
        fileStorageService.deleteIfExists(normalized);
    }

    private boolean isReferenced(String relativePath, Long excludeArticleId,
                                   String excludeNewArticleCover, String excludeNewArticleMarkdown,
                                   Long excludeProjectId, String excludeNewProjectCover,
                                   Collection<String> excludeNewProjectScreenshots) {
        String fileName = extractFileName(relativePath);
        String publicUrl = fileStorageService.toPublicUrl(relativePath);

        if (isUsedByAlbum(fileName, relativePath)) {
            return true;
        }

        List<Article> articles = articleMapper.findAllForAssetScan();
        for (Article article : articles) {
            if (sameId(excludeArticleId, article.getId())) {
                if (pathMatches(excludeNewArticleCover, fileName, relativePath)) {
                    return true;
                }
                if (markdownReferences(excludeNewArticleMarkdown, fileName, publicUrl)) {
                    return true;
                }
                continue;
            }
            if (pathMatches(article.getCoverPath(), fileName, relativePath)) {
                return true;
            }
            String markdown = safeReadMarkdown(article);
            if (markdownReferences(markdown, fileName, publicUrl)) {
                return true;
            }
        }

        List<Project> projects = projectMapper.findAllSorted();
        for (Project project : projects) {
            if (sameId(excludeProjectId, project.getId())) {
                if (pathMatches(excludeNewProjectCover, fileName, relativePath)) {
                    return true;
                }
                if (urlListReferences(excludeNewProjectScreenshots, fileName, publicUrl)) {
                    return true;
                }
                continue;
            }
            if (pathMatches(project.getCover(), fileName, relativePath)) {
                return true;
            }
            if (urlListReferences(readStringListJson(project.getScreenshotsJson()), fileName, publicUrl)) {
                return true;
            }
        }
        return false;
    }

    private List<String> readStringListJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private boolean urlListReferences(Collection<String> urls, String fileName, String publicUrl) {
        if (urls == null || urls.isEmpty()) {
            return false;
        }
        for (String url : urls) {
            Optional<String> managed = toManagedRelativePath(url);
            if (managed.isPresent() && extractFileName(managed.get()).equals(fileName)) {
                return true;
            }
            if (StringUtils.hasText(url)
                    && (url.contains(fileName) || (publicUrl != null && url.contains(publicUrl)))) {
                return true;
            }
        }
        return false;
    }

    private String safeReadMarkdown(Article article) {
        try {
            String content = fileStorageService.readMarkdown(article.getContentPath());
            if (StringUtils.hasText(content)) {
                return content;
            }
            ArticleType type = ArticleType.fromValue(article.getArticleType());
            String canonical = type.canonicalContentPath(article.getSlug());
            if (!canonical.equals(article.getContentPath())) {
                content = fileStorageService.readMarkdown(canonical);
                if (StringUtils.hasText(content)) {
                    return content;
                }
            }
            if (type == ArticleType.DOC) {
                String legacyBlog = ArticleType.BLOG.canonicalContentPath(article.getSlug());
                if (!legacyBlog.equals(article.getContentPath())) {
                    return fileStorageService.readMarkdown(legacyBlog);
                }
            }
            return "";
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private boolean sameId(Long left, Long right) {
        if (left == null || right == null) {
            return false;
        }
        return left.longValue() == right.longValue();
    }

    private boolean isUsedByAlbum(String fileName, String relativePath) {
        List<Photo> photos = photoMapper.findAll(null, null, null);
        for (Photo photo : photos) {
            if (pathMatches(photo.getFilePath(), fileName, relativePath)) {
                return true;
            }
        }
        return false;
    }

    private boolean pathMatches(String storedPath, String fileName, String relativePath) {
        if (!StringUtils.hasText(storedPath)) {
            return false;
        }
        if (isExternalUrl(storedPath)) {
            Optional<String> managed = toManagedRelativePath(storedPath);
            if (managed.isEmpty()) {
                return false;
            }
            String managedPath = managed.get();
            return managedPath.equals(relativePath)
                    || extractFileName(managedPath).equals(fileName);
        }
        String normalized = normalizeRelativePath(storedPath);
        if (normalized == null) {
            return storedPath.contains(fileName);
        }
        return normalized.equals(relativePath)
                || extractFileName(normalized).equals(fileName);
    }

    private boolean markdownReferences(String markdown, String fileName, String publicUrl) {
        if (!StringUtils.hasText(markdown)) {
            return false;
        }
        for (String url : extractUrls(markdown)) {
            Optional<String> managed = toManagedRelativePath(url);
            if (managed.isPresent() && extractFileName(managed.get()).equals(fileName)) {
                return true;
            }
            if (url.contains(fileName) || (publicUrl != null && url.contains(publicUrl))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractUrls(String markdown) {
        Set<String> urls = new HashSet<>();
        for (Pattern pattern : List.of(MARKDOWN_IMAGE, HTML_IMAGE)) {
            Matcher matcher = pattern.matcher(markdown);
            while (matcher.find()) {
                String url = matcher.group(1).trim();
                if (StringUtils.hasText(url)) {
                    urls.add(stripTitle(url));
                }
            }
        }
        return urls;
    }

    private String stripTitle(String url) {
        int space = url.indexOf(' ');
        return space > 0 ? url.substring(0, space) : url;
    }

    private void addManagedPath(Set<String> paths, String urlOrPath) {
        toManagedRelativePath(urlOrPath).ifPresent(paths::add);
    }

    private Optional<String> toManagedRelativePath(String urlOrPath) {
        if (!StringUtils.hasText(urlOrPath)) {
            return Optional.empty();
        }
        String value = urlOrPath.trim().replace("\\", "/");

        if (value.startsWith("http://") || value.startsWith("https://")) {
            String marker = storageProperties.getPublicUrlPrefix() + "/images/";
            int idx = value.indexOf(marker);
            if (idx < 0) {
                return Optional.empty();
            }
            String fileName = value.substring(idx + marker.length());
            return Optional.of(normalizeRelativePath(storageProperties.getUploadDir() + "/" + fileName));
        }

        if (value.startsWith(storageProperties.getPublicUrlPrefix() + "/images/")) {
            String fileName = value.substring((storageProperties.getPublicUrlPrefix() + "/images/").length());
            return Optional.of(normalizeRelativePath(storageProperties.getUploadDir() + "/" + fileName));
        }

        String normalized = fileStorageService.normalizeStoredPath(value);
        if (!StringUtils.hasText(normalized)) {
            return Optional.empty();
        }
        normalized = normalized.replace("\\", "/");
        String uploadDir = storageProperties.getUploadDir().replace("\\", "/");
        if (normalized.startsWith(uploadDir + "/") || normalized.contains("/images/")) {
            return Optional.of(normalizeRelativePath(normalized));
        }
        return Optional.empty();
    }

    private String normalizeRelativePath(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        return path.replace("\\", "/").replaceAll("^/+", "").toLowerCase(Locale.ROOT);
    }

    private boolean isExternalUrl(String path) {
        String value = path.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String extractFileName(String path) {
        String normalized = path.replace("\\", "/");
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }
}
