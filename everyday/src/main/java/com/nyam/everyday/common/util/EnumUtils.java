package com.nyam.everyday.common.util;

/**
 *
 * ENUM 타입을 String으로 변환하는 class
 *
 * @author : 이지은
 * @fileName : EnumUtils
 * @since : 25. 8. 7.
 *
 */
public class EnumUtils {
    // null-safe & 잘못된 값-safe enum 변환
    public static <E extends Enum<E>> E safeValueOf(Class<E> enumClass, String name, E defaultValue) {
        if (name == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}