package com.ai.agents.orchestrator.workflow;

import com.ai.agents.orchestrator.node.Node;
import com.ai.agents.orchestrator.util.EasyTree;
import com.ai.agents.orchestrator.util.EasyTree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * 作为工作流的一个操控器, 职责:
 * 1. 组装工作流
 *
 * @author han
 * @time 2025/7/29 17:52
 */

public class WorkFlowManager<IN> {

    private final Logger log = LoggerFactory.getLogger(WorkFlowManager.class);
    private EasyTree nodes;

    private Map<UUID, NodeResult> resultPool;
    private TreeNode indexNode;

    private IN input;


    public IN getInput() {
        return input;
    }

    private final ExecutorService executor;

    private WorkFlowManager(ExecutorService executorService) {
        nodes = new EasyTree();
        resultPool = new ConcurrentHashMap<>();
        // 创建固定大小的线程池，可以根据实际需求调整大小
        this.executor = executorService != null ? executorService :
            new ThreadPoolExecutor(
                    4, 10, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(50),
                    r -> new Thread(r, "wf-thread-" + Thread.currentThread().getId()),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
    }



    
    public TreeNode setStartNode(Node node) {
        return nodes.setRoot(node);
    }

    
    public Object startWorkFlow() {

        nodes.forEach((node -> {
            node.setWorkFlowManager(this);
        }));

        // 获取根节点
        TreeNode root = nodes.getRoot();
        if (root == null) {
            return null;
        }
        log.info("start workflow");
        // 启动工作流，并等待其所有分支执行完成
        executeWorkflow(root).join();
        log.info("end workflow");

        this.nodes.printTree();

        return resultPool;
    }

    public Map<UUID, NodeResult> getResultPool() {
        return resultPool;
    }

    public Object startWorkFlow(IN input) {
        this.input = input;

        // 获取根节点
        TreeNode root = nodes.getRoot();
        if (root == null) {
            return null;
        }
        log.info("start workflow");
        // 启动工作流，并等待其所有分支执行完成
        executeWorkflow(root).join();
        log.info("end workflow");

        return resultPool;
    }

    /**
     * 采用深度优先的并发模型执行工作流。
     * 每个节点执行完毕后，会为其所有满足路由条件的子节点分别创建新的并发分支。
     *
     * @param node 当前要执行的节点。
     * @return 一个 CompletableFuture，代表该节点及其所有后续分支的执行状态。
     */
    private CompletableFuture<Void> executeWorkflow(TreeNode node) {

        // 1. 异步执行当前节点
        return CompletableFuture.supplyAsync(() -> {
            Object result = null;
            try {
                result = node.getElement().executeNode();
                log.info("node result: {}", result);

                // 将结果安全地放入结果池
                resultPool.put(node.getId(), new NodeResult(result));

            } catch (Exception e) {
                log.error("node throw exception: {}", result);
                // 将结果安全地放入结果池
                resultPool.put(node.getId(), new NodeResult(e.getMessage()));
                throw new RuntimeException(e);
            }
            return null;

        }, executor)
        .thenCompose(v -> {
            // 3. 根据路由条件获取所有符合条件的子节点
            List<TreeNode> nextNodes = node.getNextNodes(resultPool);

            // 如果没有后续节点，则该分支执行完毕
            if (nextNodes.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }

            // 4. 为每个子节点创建一个新的递归执行任务，并收集它们的 Future
            List<CompletableFuture<Void>> childFutures = nextNodes.stream()
                    .map(this::executeWorkflow)
                    .toList();

            // 5. 返回一个组合的 Future，它将在所有子分支都执行完毕后完成
            return CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]));
        });
    }


    public static Builder builder() {
        return new Builder();
    }

    /**
     * 从结果池中安全地获取指定节点的结果。
     */
    public <T> T getResult(TreeNode node, Class<T> type) {
        NodeResult nodeResult = resultPool.get(node);
        if (nodeResult == null) {
            throw new IllegalArgumentException("node not found in result pool");
        }

        if (nodeResult.getValue() == null) {
            log.warn("having a node returns null: {}", node.getElement());
        } else {

            Object value = nodeResult.getValue();
            if (type.isInstance(value)) {
                return type.cast(value);
            }
        }
        throw new IllegalArgumentException("node result type not match");
    }


    public static class NodeResult {
        private final Object value;
        private final Class<?> type;
        
        public NodeResult(Object value) {
            this.value = value;
            this.type = value.getClass();
        }
        
        public Object getValue() { return value; }
        public Class<?> getType() { return type; }
    }

    public static class Builder {
        private ExecutorService executorService;

        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }
        public WorkFlowManager build() {
            return new WorkFlowManager(executorService);
        }
    }

}
