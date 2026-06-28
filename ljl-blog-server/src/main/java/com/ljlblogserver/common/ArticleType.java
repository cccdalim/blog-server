package com.ljlblogserver.common;

import lombok.Getter;

@Getter
public enum ArticleType {

    BLOG("blog", "content/blog"),
    DOC("doc", "content/docs");

    private final String value;
    private final String contentDir;

    ArticleType(String value, String contentDir) {
        this.value = value;
        this.contentDir = contentDir;
    }
}
