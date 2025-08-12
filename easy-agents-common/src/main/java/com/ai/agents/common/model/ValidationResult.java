package com.ai.agents.common.model;

import java.util.*;

/**
 * 工作流校验结果
 * @author han
 * @time 2025/8/11 14:08
 */

public class ValidationResult {
    private final boolean ok;
    private final String message;
    private final List<String> warnings;

    public ValidationResult(boolean ok, String message, List<String> warnings) {
        this.ok = ok;
        this.message = message;
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public boolean isOk() { return ok; }
    public String getMessage() { return message; }
    public List<String> getWarnings() { return warnings; }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "ok=" + ok +
                ", message='" + message + '\'' +
                ", warnings=" + warnings +
                '}';
    }
}
