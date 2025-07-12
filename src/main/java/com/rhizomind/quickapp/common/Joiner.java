package com.rhizomind.quickapp.common;

import java.util.List;

public class Joiner {

    public static String join(List<String> args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        return builder.toString();
    }
}
