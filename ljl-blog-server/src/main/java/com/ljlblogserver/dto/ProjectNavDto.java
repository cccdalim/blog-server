package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectNavDto {

    private ProjectNavItemDto prev;
    private ProjectNavItemDto next;
}
