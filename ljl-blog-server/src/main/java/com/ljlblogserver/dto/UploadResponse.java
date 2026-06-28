package com.ljlblogserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {

    private String url;
    private String path;
}
