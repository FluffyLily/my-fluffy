package com.renee328.enums;

public interface BaseEnum {
    String getValue();

    default String normalizedValue() {
        if (getValue() == null) return null;
        return getValue().replace("_", "").toLowerCase();
    }

    static String normalize(String input) {
        if (input == null) return null;
        return input.replace("_", "").toLowerCase();
    }
}