package com.myrosh.erowl;

import org.apache.commons.lang3.StringUtils;

/**
 * @author igorm
 *
 * A utilities class
 *
 */
public class Utils
{
    public static String lowerCaseCleanName(String name) {
        return StringUtils.lowerCase(cleanName(name));
    }

    public static String capitalizeCleanName(String name) {
        return StringUtils.capitalize(cleanName(name));
    }

    public static String uncapitalizeCleanName(String name) {
        return StringUtils.uncapitalize(cleanName(name));
    }

    public static String cleanName(String name) {
        return StringUtils.isBlank(name) ? "" : name.replaceAll("[^\\p{L}\\p{Nd}]+", "");
    }
}
