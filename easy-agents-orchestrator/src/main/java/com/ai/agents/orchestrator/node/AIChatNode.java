package com.ai.agents.orchestrator.node;

import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.util.*;
import reactor.core.publisher.Flux;

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
    public <OUT> OUT executeBlocking() {

        List<Message> messages = prompt.apply(input);

        if (ClassUtils.isAssignable(outType, String.class)) {
            return (OUT) chatClientRequestSpec.messages(messages).call().content();
        }

        return (OUT) chatClientRequestSpec.messages(messages).call().entity(outType);
    }

    @Override
    public Flux<?> executeStreaming() {
        // 真实流式：直接返回底层的响应式流
        List<Message> messages = prompt.apply(input);
        return chatClientRequestSpec.messages(messages).stream().content();
    }

    private AIChatNode(AIChatNodeBuilder<IN> builder) {
        super(builder.input);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private AIChatNode(AIChatNodeBuilder<IN> builder, UUID inputResultId) {
        super(inputResultId);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }



    public static <IN> AIChatNodeBuilder<IN> builder() {
        return new AIChatNodeBuilder<>();
    }

    public static class AIChatNodeBuilder<IN> extends NodeBuilder<IN, AIChatNodeBuilder<IN>, AIChatNode<IN>> {

        private ChatClientRequestSpec chatClientRequestSpec;
        private Function<IN, List<Message>> prompt = input -> null;
        public AIChatNodeBuilder<IN> prompt(Function<IN, List<Message>> prompt) {
            this.prompt = prompt;
            return this;
        }
        public AIChatNodeBuilder<IN> chatClientRequestSpec(ChatClientRequestSpec chatClientRequestSpec) {
            this.chatClientRequestSpec = chatClientRequestSpec;
            return this;
        }

        @Override
        public AIChatNode<IN> build(UUID inputResultId) {
            // 先验证参数
            validate();
            this.inputResultId(inputResultId);
            return new AIChatNode<IN>(this, inputResultId);
        }

        @Override
        public AIChatNode<IN> build(IN input) {
            // 先验证参数
            validate();
            this.input(input);
            return new AIChatNode<IN>(this);
        }

    }
}
