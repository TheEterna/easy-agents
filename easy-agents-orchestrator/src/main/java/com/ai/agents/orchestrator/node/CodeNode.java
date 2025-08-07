package com.ai.agents.orchestrator.node;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author han
 * @time 2025/7/29 11:33
 */
public class CodeNode<IN ,OUT> extends Node<IN ,OUT> implements Callable<OUT> {

    private Function<IN, OUT> code = input -> null;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public OUT call() throws Exception {
        return code.apply(input);
    }

    @Override
    public OUT executeNode() {
        // 使用存储的input作为参数调用函数
        return code.apply(input);
    }

    private CodeNode(CodeNodeBuilder<IN, OUT> builder) {
        super(builder.input);
        this.code = builder.code;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private CodeNode(CodeNodeBuilder<IN, OUT> builder, UUID resultId) {
        super(builder.resultId);
        this.code = builder.code;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    /**
     * 静态方法获取建造器
     */

    public static <IN, OUT> CodeNodeBuilder<IN, OUT> builder() {
        return new CodeNodeBuilder<>();
    }

    // 具体建造器实现
    public static class CodeNodeBuilder<IN, OUT> extends NodeBuilder<IN, OUT, CodeNode<IN, OUT>> {
        Function<IN, OUT> code;

        public CodeNodeBuilder<IN, OUT> code(Function<IN, OUT> code) {
            this.code = code;
            return this;
        }

        @Override
        public CodeNode<IN, OUT> build(UUID resultId) {
            // 先验证参数
            validate();
            this.resultId(resultId);
            return new CodeNode<IN, OUT>(this, resultId);
        }
        @Override
        public CodeNode<IN, OUT> build(IN input) {
            // 先验证参数
            validate();
            this.input(input);
            return new CodeNode<>(this);
        }
    }
}
