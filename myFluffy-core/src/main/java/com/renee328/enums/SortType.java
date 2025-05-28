package com.renee328.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum SortType implements BaseEnum {

    RECENT("recent"),
    OLD("old");

    private final String value;

    SortType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static final Map<String, SortType> VALUE_MAP = new HashMap<>();

    static {
        for (SortType type : SortType.values()) {
            VALUE_MAP.put(BaseEnum.normalize(type.value), type);
            VALUE_MAP.put(BaseEnum.normalize(type.name()), type);
        }
    }

    @JsonCreator
    public static SortType from(String input) {
        if (input == null) {
            return RECENT; // 기본값
        }
        return VALUE_MAP.getOrDefault(BaseEnum.normalize(input), RECENT);
    }
}