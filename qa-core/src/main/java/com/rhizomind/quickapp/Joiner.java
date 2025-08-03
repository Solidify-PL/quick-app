package com.rhizomind.quickapp;

import java.util.List;

public class Joiner {

    public static String join(List<String> args) {
        return join(args, " ");
    }

    private static String join(List<String> args, String separator) {
        if (args == null || args.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {

            builder.append(arg).append(separator);
        }
        return builder.toString();
    }
}
