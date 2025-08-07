package com.ai.agents.orchestrator.node;

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
    public <OUT> OUT executeNode() {
        // 使用存储的input作为参数调用函数
        return (OUT) code.apply(input);
    }

    private CodeNode(CodeNodeBuilder<IN> builder) {
        super(builder.input);
        this.code = builder.code;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private CodeNode(CodeNodeBuilder<IN> builder, UUID resultId) {
        super(builder.resultId);
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
    public static class CodeNodeBuilder<IN> extends NodeBuilder<IN, CodeNode<IN>> {
        Function<IN, Object> code;

        public CodeNodeBuilder<IN> code(Function<IN, Object> code) {
            this.code = code;
            return this;
        }

        @Override
        public CodeNode<IN> build(UUID resultId) {
            // 先验证参数
            validate();
            this.resultId(resultId);
            return new CodeNode<IN>(this, resultId);
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
