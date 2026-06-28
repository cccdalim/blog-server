package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocNavDto {

    private NavItemDto prev;
    private NavItemDto next;
}
