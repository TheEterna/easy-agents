package com.ai.agents.orchestrator.node;

import com.ai.agents.orchestrator.node.AIChatNode.*;
import com.ai.agents.orchestrator.node.AIChatNode.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;


/**
 * 对话AI节点
 * 支持文本对话、上下文处理
 * 
 * @author han
 * @time 2025/8/4 16:49
 */
public class AIChatNode<IN, OUT> extends Node<IN, OUT> {


    private ChatClientRequestSpec chatClientRequestSpec;
    private Function<IN, List<Message>> prompt = input -> null;

    @Override
    public OUT executeNode() {
        return chatClientRequestSpec.call().entity(outType);
    }


    private AIChatNode(AIChatNodeBuilder<IN, OUT> builder) {
        super(builder.input);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    private AIChatNode(AIChatNodeBuilder<IN, OUT> builder, UUID resultId) {
        super(builder.resultId);
        this.prompt = builder.prompt;
        this.chatClientRequestSpec = builder.chatClientRequestSpec;
        this.inType = builder.inType;
        this.outType = builder.outType;
        this.workFlowManager = builder.workFlowManager;
    }

    // 静态方法获取建造器
    public static <IN, OUT> AIChatNodeBuilder<IN, OUT> builder() {
        return new AIChatNodeBuilder<>();
    }

    public static class AIChatNodeBuilder<IN, OUT> extends NodeBuilder<IN, OUT, AIChatNode<IN, OUT>> {
        private ChatClientRequestSpec chatClientRequestSpec;
        private Function<IN, List<Message>> prompt = input -> null;
        public AIChatNode.AIChatNodeBuilder<IN, OUT> prompt(Function<IN, List<Message>> prompt) {
            this.prompt = prompt;
            return this;
        }
        public AIChatNode.AIChatNodeBuilder<IN, OUT> chatClientRequestSpec(ChatClientRequestSpec chatClientRequestSpec) {
            this.chatClientRequestSpec = chatClientRequestSpec;
            return this;
        }

        @Override
        public AIChatNode<IN, OUT> build(UUID resultId) {
            // 先验证参数
            validate();
            this.resultId(resultId);
            return new AIChatNode<IN, OUT>(this, resultId);
        }
        @Override
        public AIChatNode<IN, OUT> build(IN input) {
            // 先验证参数
            validate();
            this.input(input);
            return new AIChatNode<IN, OUT>(this);
        }
    }

}
