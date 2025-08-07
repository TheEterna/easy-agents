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

import static com.ai.agents.orchestrator.node.CodeNode.builder;

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


        // 1. 用户输入处理节点
        CodeNode<String, String> inputProcessor = CodeNode.<String, String>builder()
                .code(input -> "处理用户输入: " + input)
                .build("666");


        // 2. 构造AI对话节点的请求
        ChatClientRequestSpec requestSpec = chatClient.prompt().system("你是一个乐于助人的小助手");

        // 3. AI对话节点
        AIChatNode<String, String> aiChatNode = AIChatNode.<String, String>builder()
                .chatClientRequestSpec(requestSpec)
                .prompt(input -> List.of(new UserMessage(input)))
                .outType(String.class)
                .build("请问爱到底是什么");
        // 构建树结构
        EasyTree.TreeNode root = manager.setStartNode(inputProcessor);
        root.addChild(aiChatNode);

        // 启动工作流
        Object result = manager.startWorkFlow();

        System.out.println("工作流执行结果: " + result);

    }
}
