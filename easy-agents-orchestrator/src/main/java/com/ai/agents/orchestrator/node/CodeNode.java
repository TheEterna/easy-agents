package com.ai.agents.orchestrator.node;

import reactor.core.publisher.Flux;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author han
 * @time 2025/7/29 11:33
 */
public class CodeNode<IN> extends Node<IN> {

    private Function<IN, Object> code = input -> null;

    @Override
    public <OUT> OUT executeBlocking() {
        // 使用存储的input作为参数调用函数
        return (OUT) code.apply(input);
    }

    @Override
    public Flux<?> executeStreaming() {
        Object v = this.executeBlocking();
        return Flux.just(v);
    }

    private CodeNode(CodeNodeBuilder<IN> builder) {
        super(builder.input);
        this.code = builder.code;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private CodeNode(CodeNodeBuilder<IN> builder, UUID inputResultId) {
        super(builder.inputResultId);
        this.code = builder.code;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    /**
     * 静态方法获取建造器
     */

    public static <IN> CodeNodeBuilder<IN> builder() {
        return new CodeNodeBuilder<>();
    }

    // 具体建造器实现
    public static class CodeNodeBuilder<IN> extends NodeBuilder<IN, CodeNodeBuilder<IN>, CodeNode<IN>> {
        Function<IN, Object> code;

        public CodeNodeBuilder<IN> code(Function<IN, Object> code) {
            this.code = code;
            return this;
        }

        @Override
        public CodeNode<IN> build(UUID inputResultId) {
            // 先验证参数
            validate();
            this.inputResultId(inputResultId);
            return new CodeNode<IN>(this, inputResultId);
        }
        @Override
        public CodeNode<IN> build(IN input) {
            // 先验证参数
            validate();
            this.input(input);
            return new CodeNode<>(this);
        }
    }
}
