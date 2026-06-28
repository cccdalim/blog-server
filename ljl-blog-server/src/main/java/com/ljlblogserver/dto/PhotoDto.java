package com.ljlblogserver.dto;

import lombok.Data;

@Data
public class PhotoDto {

    private String id;
    private String url;
    private String title;
    private String city;
    private String country;
    private String category;
    private String date;
    private Integer width;
    private Integer height;
}
