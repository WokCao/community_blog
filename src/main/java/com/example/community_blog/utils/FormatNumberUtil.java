package com.example.community_blog.utils;

public class FormatNumberUtil {
    public static String formatNumber(Long number) {
        if (number == null) return "0";

        if (number >= 1_000_000_000) {
            return String.format("%.1fB+", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM+", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK+", number / 1_000.0);
        } else {
            return number.toString();
        }
    }
}
