package com.ljlblogserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String root = "./data";
    private String contentDir = "content/blog";
    private String uploadDir = "uploads/images";
    private String publicUrlPrefix = "/uploads";
}
