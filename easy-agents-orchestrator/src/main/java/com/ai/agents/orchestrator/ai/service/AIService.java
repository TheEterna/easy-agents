package com.ai.agents.orchestrator.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务封装层
 * 为AI节点提供统一的AI能力调用接口
 * 
 * @author 父亲的首席架构师
 * @time 2025/8/4
 */
@Service
public class AIService {


    private ChatClient chatClient;

    public AIService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 通用对话接口
     */
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    /**
     * 高级对话接口，支持上下文
     */
    public String chatWithContext(List<Message> messages) {
        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        return response.getResult().getOutput().getText();
    }

    /**
     * 流式对话接口
     */
    public String streamChat(String message) {
        StringBuilder response = new StringBuilder();
        Flux<String> flux = this.chatClient.prompt(message)
                .stream()
                .content();
        String content = flux.collectList().block().stream().collect(Collectors.joining());

        return content;
    }
}
