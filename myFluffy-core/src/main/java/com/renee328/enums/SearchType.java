package com.renee328.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum SearchType implements BaseEnum {

    TITLE_CONTENT("titleContent"),
    TITLE("title"),
    CONTENT("content"),
    AUTHOR_NAME("authorName"),
    POST_CATEGORY("postCategory"),
    USER_NAME("userName"),
    LOGIN_ID("loginId"),
    EMAIL("email");

    private final String value;

    SearchType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static final Map<String, SearchType> VALUE_MAP = new HashMap<>();

    static {
        for (SearchType type : SearchType.values()) {
            VALUE_MAP.put(BaseEnum.normalize(type.value), type);
            VALUE_MAP.put(BaseEnum.normalize(type.name()), type);
        }
    }

    @JsonCreator
    public static SearchType from(String input) {
        if (input == null) {
            return null;
        }
        return VALUE_MAP.get(BaseEnum.normalize(input));
    }
}