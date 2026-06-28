package com.ljlblogserver.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrphanImageCleanupDto {

    private int deletedCount;
    private long freedBytes;
    private List<String> deleted = new ArrayList<>();
}
