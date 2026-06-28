package com.ljlblogserver.service;

import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.dto.DashboardStatsDto;
import com.ljlblogserver.mapper.ArticleMapper;
import com.ljlblogserver.mapper.CategoryMapper;
import com.ljlblogserver.mapper.PhotoMapper;
import com.ljlblogserver.mapper.ProjectMapper;
import com.ljlblogserver.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ArticleMapper articleMapper;
    private final ProjectMapper projectMapper;
    private final PhotoMapper photoMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final FileStorageService fileStorageService;
    private final ManagedUploadCleanupService uploadCleanupService;

    public DashboardStatsDto getStats() throws IOException {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setBlogCount(articleMapper.count(ArticleType.BLOG.getValue(), null, null, null));
        stats.setDocCount(articleMapper.count(ArticleType.DOC.getValue(), null, null, null));
        stats.setProjectCount(projectMapper.countAll());
        stats.setPhotoCount(photoMapper.count(null, null, null));
        stats.setCategoryCount(categoryMapper.findAllWithCount().size());
        stats.setTagCount(tagMapper.findAllWithCount().size());

        Path uploadRoot = fileStorageService.getUploadRootPath();
        int fileCount = 0;
        long totalSize = 0;
        if (Files.isDirectory(uploadRoot)) {
            try (var stream = Files.list(uploadRoot)) {
                for (Path file : stream.filter(Files::isRegularFile).toList()) {
                    fileCount++;
                    totalSize += Files.size(file);
                }
            }
        }
        stats.setUploadFileCount(fileCount);
        stats.setUploadSizeBytes(totalSize);

        var orphanScan = uploadCleanupService.scanOrphanImages();
        stats.setOrphanImageCount(orphanScan.getOrphanCount());
        stats.setOrphanSizeBytes(orphanScan.getOrphanSizeBytes());
        return stats;
    }
}
