package com.sims.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计算机科学技术专业当前支持的科目（每科仅一次成绩）。
 */
public final class SubjectCatalog {

    public static final String C_LANGUAGE = "C语言";
    public static final String CET4 = "英语四级";

    private static final Map<String, BigDecimal> SUBJECT_MAX_SCORES = new LinkedHashMap<>();

    static {
        SUBJECT_MAX_SCORES.put(C_LANGUAGE, new BigDecimal("100.00"));
        SUBJECT_MAX_SCORES.put(CET4, new BigDecimal("710.00"));
    }

    private SubjectCatalog() {}

    public static Set<String> names() {
        return SUBJECT_MAX_SCORES.keySet();
    }

    public static List<String> orderedNames() {
        return List.copyOf(SUBJECT_MAX_SCORES.keySet());
    }

    public static boolean isSupported(String subject) {
        return subject != null && SUBJECT_MAX_SCORES.containsKey(subject.trim());
    }

    public static BigDecimal maxScoreOf(String subject) {
        if (!isSupported(subject)) {
            throw new IllegalArgumentException("不支持的科目: " + subject + "，仅支持: " + orderedNames());
        }
        return SUBJECT_MAX_SCORES.get(subject.trim());
    }

    public static void requireSupported(String subject) {
        if (!isSupported(subject)) {
            throw new IllegalArgumentException("不支持的科目: " + subject + "，仅支持: " + orderedNames());
        }
    }
}
