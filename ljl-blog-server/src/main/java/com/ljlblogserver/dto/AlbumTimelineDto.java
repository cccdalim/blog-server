package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlbumTimelineDto {

    private String date;
    private String title;
    private String description;
}
