package com.xinian.contingameime.client;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Utility to detect text field widgets by class name pattern.
 * Ported from IMBlocker's Common.classIsTextField approach.
 */
public class TextFieldDetector {
    private static final Pattern TEXT_FIELD_PATTERN = Pattern.compile(
            ".*(TextField|EditBox|EditText|TextInput|SearchField|InputField)[^.]*$",
            Pattern.CASE_INSENSITIVE);
    private static final HashMap<Class<?>, Boolean> cache = new HashMap<>();

    public static boolean isTextField(Class<?> c) {
        if (c == null) return false;
        if (cache.containsKey(c)) return cache.get(c);
        boolean result;
        if (TEXT_FIELD_PATTERN.matcher(c.getName()).matches()) {
            result = true;
        } else {
            result = isTextField(c.getSuperclass());
        }
        cache.put(c, result);
        return result;
    }
}

