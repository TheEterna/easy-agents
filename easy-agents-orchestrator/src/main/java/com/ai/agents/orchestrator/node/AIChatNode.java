package com.ai.agents.orchestrator.node;

import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.messages.*;

import java.util.*;
import java.util.function.*;


/**
 * 对话AI节点
 * 支持文本对话、上下文处理
 * 
 * @author han
 * @time 2025/8/4 16:49
 */
public class AIChatNode<IN> extends Node<IN> {


    private ChatClientRequestSpec chatClientRequestSpec;
    private Function<IN, List<Message>> prompt = input -> null;


    @Override
    public <OUT> OUT executeNode() {
        return (OUT) chatClientRequestSpec.call().entity(outType);
    }
//    @Override
//    public <OUT> OUT executeNode(Class<OUT> outType) {
//        return chatClientRequestSpec.call().entity(outType);
//    }


    private AIChatNode(AIChatNodeBuilder<IN> builder) {
        super(builder.input);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private AIChatNode(AIChatNodeBuilder<IN> builder, UUID resultId) {
        super(builder.resultId);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    // 静态方法获取建造器
//    public static <IN, OUT> AIChatNodeBuilder<IN, OUT> builder() {
//        return new AIChatNodeBuilder<>();
//    }

    // 静态方法获取建造器
    public static <IN> AIChatNodeBuilder<IN> builder() {
        return new AIChatNodeBuilder<>();
    }

    public static class AIChatNodeBuilder<IN> extends NodeBuilder<IN, AIChatNode<IN>> {
        private ChatClientRequestSpec chatClientRequestSpec;
        private Function<IN, List<Message>> prompt = input -> null;
        public AIChatNode.AIChatNodeBuilder<IN> prompt(Function<IN, List<Message>> prompt) {
            this.prompt = prompt;
            return this;
        }
        public AIChatNode.AIChatNodeBuilder<IN> chatClientRequestSpec(ChatClientRequestSpec chatClientRequestSpec) {
            this.chatClientRequestSpec = chatClientRequestSpec;
            return this;
        }

        @Override
        public AIChatNode<IN> build(UUID resultId) {
            // 先验证参数
            validate();
            this.resultId(resultId);
            return new AIChatNode<IN>(this, resultId);
        }
        @Override
        public AIChatNode<IN> build(IN input) {
            // 先验证参数
            validate();
            this.input(input);
            return new AIChatNode<IN>(this);
        }

    }

//    public static class AIChatNodeBuilder<IN, OUT> extends NodeBuilder<IN, OUT, AIChatNode<IN, OUT>> {
//        private ChatClientRequestSpec chatClientRequestSpec;
//        private Function<IN, List<Message>> prompt = input -> null;
//        public AIChatNode.AIChatNodeBuilder<IN, OUT> prompt(Function<IN, List<Message>> prompt) {
//            this.prompt = prompt;
//            return this;
//        }
//        public AIChatNode.AIChatNodeBuilder<IN, OUT> chatClientRequestSpec(ChatClientRequestSpec chatClientRequestSpec) {
//            this.chatClientRequestSpec = chatClientRequestSpec;
//            return this;
//        }
//
//        @Override
//        public AIChatNode<IN, OUT> build(UUID resultId) {
//            // 先验证参数
//            validate();
//            this.resultId(resultId);
//            return new AIChatNode<IN, OUT>(this, resultId);
//        }
//        @Override
//        public AIChatNode<IN, OUT> build(IN input) {
//            // 先验证参数
//            validate();
//            this.input(input);
//            return new AIChatNode<IN, OUT>(this);
//        }
//    }

}
