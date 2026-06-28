package com.ljlblogserver.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Photo {

    private Long id;
    private String filePath;
    private String title;
    private String city;
    private String country;
    private String category;
    private LocalDate photoDate;
    private Integer width;
    private Integer height;
    private LocalDateTime createdAt;
}
