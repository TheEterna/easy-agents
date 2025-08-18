package com.ai.agents.orchestrator.util;

/**
 * @author han
 * @time 2025/8/17 22:56
 */

public class NodeResult {

    private final Object value;
    private final Class<?> type;

    public NodeResult(Object value) {
        this.value = value;
        this.type = value.getClass();
    }

    public Object getValue() { return value; }
    public Class<?> getType() { return type; }

    @Override
    public String toString() {
        return "NodeResult{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}
