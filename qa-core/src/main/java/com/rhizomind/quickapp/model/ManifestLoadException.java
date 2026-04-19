package com.rhizomind.quickapp.model;

import java.io.IOException;

public class ManifestLoadException extends IOException {

    public enum Kind {
        INVALID_YAML,
        INVALID_SCHEMA
    }

    private final Kind kind;
    private final String source;
    private final int line;
    private final int column;
    private final String fieldPath;
    private final String detail;

    public ManifestLoadException(Kind kind, String source, int line, int column,
                                 String fieldPath, String detail, Throwable cause) {
        super(buildMessage(kind, source, line, column, fieldPath, detail), cause);
        this.kind = kind;
        this.source = source;
        this.line = line;
        this.column = column;
        this.fieldPath = fieldPath;
        this.detail = detail;
    }

    public Kind getKind() {
        return kind;
    }

    public String getSource() {
        return source;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public String getDetail() {
        return detail;
    }

    private static String buildMessage(Kind kind, String source, int line, int column,
                                       String fieldPath, String detail) {
        StringBuilder sb = new StringBuilder();
        sb.append(kind == Kind.INVALID_YAML
                ? "Manifest is not valid YAML"
                : "Manifest does not match the expected schema");
        if (source != null && !source.isEmpty()) {
            sb.append(" in ").append(source);
        }
        if (line > 0) {
            sb.append(" at line ").append(line);
            if (column > 0) {
                sb.append(", column ").append(column);
            }
        }
        if (fieldPath != null && !fieldPath.isEmpty()) {
            sb.append(" (field: ").append(fieldPath).append(")");
        }
        if (detail != null && !detail.isEmpty()) {
            sb.append(": ").append(detail);
        }
        return sb.toString();
    }
}
