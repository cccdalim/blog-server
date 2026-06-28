package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagDto {

    private String id;
    private String name;
    private String slug;
    private int count;
}
