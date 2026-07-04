package com.ljlblogserver.dto;

import lombok.Data;

@Data
public class DashboardStatsDto {

    private long blogCount;
    private long docCount;
    private long recipeCount;
    private long projectCount;
    private long photoCount;
    private long categoryCount;
    private long tagCount;
    private int uploadFileCount;
    private long uploadSizeBytes;
    private int orphanImageCount;
    private long orphanSizeBytes;
}
