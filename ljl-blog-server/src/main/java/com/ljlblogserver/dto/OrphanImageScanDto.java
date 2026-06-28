package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrphanImageScanDto {

    private int totalFiles;
    private int referencedCount;
    private int orphanCount;
    private long orphanSizeBytes;
    private List<OrphanImageItemDto> orphans = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrphanImageItemDto {
        private String path;
        private String url;
        private long sizeBytes;
    }
}
