package com.ljlblogserver.common;

import lombok.Getter;

@Getter
public enum ArticleType {

    BLOG("blog", "content/blog"),
    DOC("doc", "content/docs"),
    RECIPE("recipe", "content/recipes");

    private final String value;
    private final String contentDir;

    ArticleType(String value, String contentDir) {
        this.value = value;
        this.contentDir = contentDir;
    }

    public static ArticleType fromValue(String value) {
        if (value == null) {
            return BLOG;
        }
        for (ArticleType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return BLOG;
    }

    public String canonicalContentPath(String slug) {
        return contentDir + "/" + slug + ".md";
    }
}
