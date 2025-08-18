package com.ai.agents.orchestrator.workflow;

import com.ai.agents.orchestrator.Main;
import com.ai.agents.orchestrator.node.*;
import com.ai.agents.orchestrator.util.EasyTree;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * @author han
 * @time 2025/8/4 4:12
 */

@SpringBootTest(classes = Main.class)
public class WorkFlowTests {

    private final static Logger log = LoggerFactory.getLogger(WorkFlowTests.class);

    @Autowired
    private ChatClient chatClient;



    @Test
    void testAiWorkflow() {
        // 创建AI工作流
        WorkFlowManager manager = WorkFlowManager.builder()
                .executorService(java.util.concurrent.Executors.newFixedThreadPool(4))
                .build();
        // 现在使用自动类型提取，代码更简洁！
        // 1. 代码节点构建
        CodeNode<String> startNode = CodeNode.<String>builder()
                .code(input -> "用户问: " + input)
                .outType(String.class)  // 明确指定输出类型为String
                .build("我最近不开心");
        // 2. 构造AI对话节点的请求
        // 构建树结构
        EasyTree.TreeNode rootNode = manager.setStartNode(startNode);
        // 3. AI对话节点 - 明确指定输出类型
        ChatClientRequestSpec requestSpec = chatClient.prompt().system("你是一个乐于助人的小助手");
        AIChatNode<String> aiChatNode = AIChatNode.<String>builder()
                .chatClientRequestSpec(requestSpec)
                .prompt(input -> List.of(new UserMessage(input)))
                .outType(String.class)
                // 使用节点ID, 当作输入结果, 输入参数类型即为泛型
                .build(rootNode.getId());

        // 链接节点
        rootNode.addChild(aiChatNode);

        // 启动工作流
        Object result = manager.startBlocking();

        System.out.println("工作流执行结果: " + result);

    }
}
