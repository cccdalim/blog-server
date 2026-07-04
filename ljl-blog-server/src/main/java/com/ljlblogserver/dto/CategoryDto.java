package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {

    private String id;
    private String name;
    private String slug;
    private String scope;
    private int count;
}
