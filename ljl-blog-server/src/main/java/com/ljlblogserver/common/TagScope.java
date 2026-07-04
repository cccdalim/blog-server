package com.ljlblogserver.common;

import lombok.Getter;

@Getter
public enum TagScope {

    CONTENT("content"),
    RECIPE("recipe");

    private final String value;

    TagScope(String value) {
        this.value = value;
    }

    public static TagScope fromArticleType(ArticleType type) {
        return type == ArticleType.RECIPE ? RECIPE : CONTENT;
    }

    public static TagScope fromValue(String value) {
        if (RECIPE.value.equals(value)) {
            return RECIPE;
        }
        return CONTENT;
    }
}
