package com.ai.agents.orchestrator.workflow;

import com.ai.agents.orchestrator.*;
import com.ai.agents.orchestrator.node.*;
import com.ai.agents.orchestrator.util.NodeResult;
import com.ai.agents.orchestrator.util.*;
import com.ai.agents.orchestrator.util.EasyTree.*;
import org.junit.jupiter.api.Test;
import org.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流式输出路径的基础单元测试：
 * 1) 单节点执行时，会边执行边发射事件（本例为单元素）
 * 2) 节点完成后，其“最后一个元素”会写入结果池
 */

@SpringBootTest(classes = Main.class)
public class WorkFlowManagerStreamingTest {

    private final static Logger log = LoggerFactory.getLogger(WorkFlowTests.class);

    @Autowired
    private ChatClient chatClient;


    @Test
    public void testSingleNodeStreamingEmitsAndCompletes() {
        // 1. 构建工作流管理器
        WorkFlowManager<String> manager = WorkFlowManager.builder().build();

        // 1. 代码节点构建
        CodeNode<String> startNode = CodeNode.<String>builder()
                .code(input -> "用户问: " + input)
                .outType(String.class)  // 明确指定输出类型为String
                .build("我最近不开心");
        // 2. 构造AI对话节点的请求
        // 构建树结构（仅调用一次 setStartNode）
        EasyTree.TreeNode rootNode = manager.setStartNode(startNode);

        // 3. AI对话节点 - 明确指定输出类型
        ChatClientRequestSpec requestSpec = chatClient.prompt().system("你是一个乐于助人的小助手");

        AIChatNode<String> aiChatNode = AIChatNode.<String>builder()
                .chatClientRequestSpec(requestSpec)
                .prompt(input -> List.of(new UserMessage(input)))
                .outType(String.class)
                // 使用节点ID, 当作输入结果, 输入参数类型即为泛型
                .build(rootNode.getId());

        // 3. 设置开始节点与子节点（使用与 aiChatNode 相同的 rootNode 引用）
        rootNode.addChild(aiChatNode);

        // 4. 启动流式执行（添加详细打点日志）
        Flux<Object> flux = manager.startStreaming()
                .doOnSubscribe(s -> System.out.println("[TEST] subscribe to startStreaming()"))
                .doOnNext(o -> System.out.println("[TEST] onNext: " + o.toString()))
                .doOnError(e -> System.out.println("[TEST] onError: " + e))
                .doOnComplete(() -> {
                    System.out.println("[TEST] onComplete");
                    System.out.println("[TEST] resultPool size: " + manager.getResultPool().size());
                    manager.getResultPool().forEach((k, v) ->
                            System.out.println("[TEST] pool[" + k + "] = " + v.getValue()));
                });

        // 5. 仅验证流顺利完成（忽略 onNext 内容/数量，因 AI 节点输出具不稳定性）
        StepVerifier.create(flux)
                .thenConsumeWhile(o -> true)
                .verifyComplete();

        // 6. 完成后，结果池应包含两个节点的聚合结果（各自流的最后一个元素）。
        Map<UUID, NodeResult> pool = manager.getResultPool();
        assertNotNull(pool, "结果池不应为 null");
        assertEquals(2, pool.size(), "两个节点应在结果池中留下 2 条记录");
    }
}
