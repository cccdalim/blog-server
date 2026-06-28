package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilterOptionDto {

    private String label;
    private String value;
    private int count;
}
