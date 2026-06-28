package com.ljlblogserver.entity;

import lombok.Data;

@Data
public class Tag {

    private Long id;
    private String name;
    private String slug;
    private Integer count;
}
