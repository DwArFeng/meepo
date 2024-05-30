package com.dwarfeng.meepo.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 标识符工具类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class IdentifierUtil {

    /**
     * 判断指定的字符串是否是有效的标识符。
     *
     * @param str 指定的字符串。
     * @return 指定的字符串是否是有效的标识符。
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidIdentifier(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^[a-zA-Z0-9.,\\-_]+$");
    }

    private IdentifierUtil() {
        throw new IllegalStateException("禁止实例化");
    }
}
