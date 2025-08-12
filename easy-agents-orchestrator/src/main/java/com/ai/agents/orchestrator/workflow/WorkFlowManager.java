package com.ai.agents.orchestrator.workflow;

import com.ai.agents.common.model.*;
import com.ai.agents.orchestrator.node.Node;
import com.ai.agents.orchestrator.util.EasyTree;
import com.ai.agents.orchestrator.util.EasyTree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    // 聚合执行所需的状态：每个节点剩余未完成的父节点数量，以及是否被任一父节点路由命中
    private Map<TreeNode, AtomicInteger> parentsLeft; // 初始为父节点数量
    private Map<TreeNode, AtomicBoolean> allowedByAnyParent; // 任一父节点路由命中

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
        prepareAggregationState();
        // 启动工作流，并等待其所有分支执行完成
        executeWorkflow(root).join();
        log.info("end workflow");

        this.nodes.printTree();

        return resultPool;
    }

    /**
     * 校验当前工作流结构是否健康。
     * 检测项：
     * 1) 是否存在根节点；
     * 2) 是否存在循环链（不建议使用循环链，循环链可能导致程序无法退出）；
     * 3) 是否存在空节点（元素为null）；
     * 4) 是否为空工作流（只有根且无子节点）。
     * 返回 ValidationResult，包含是否正常、提示信息及可选警告列表。
     */
    public ValidationResult validateWorkflow() {
        List<String> warnings = new ArrayList<>();

        TreeNode root = nodes.getRoot();
        if (root == null) {
            return new ValidationResult(false, "未设置开始节点（根节点为空）", warnings);
        }

        // DFS 检测环 & 收集节点
        Set<TreeNode> visited = new HashSet<>();
        Set<TreeNode> onPath = new HashSet<>();
        boolean[] hasCycle = new boolean[]{false};
        boolean[] hasNullElement = new boolean[]{false};

        dfsValidate(root, visited, onPath, hasCycle, hasNullElement);

        if (hasNullElement[0]) {
            warnings.add("发现元素为null的节点（可能是占位ROOT或构建异常），建议检查");
        }
        if (visited.size() == 1 && root.getChildren().isEmpty()) {
            warnings.add("工作流为空：仅包含开始节点且没有后续节点");
        }

        if (hasCycle[0]) {
            return new ValidationResult(false, "检测到循环链：不建议使用循环链，循环链可能导致程序无法退出", warnings);
        }

        return new ValidationResult(true, warnings.isEmpty() ? "工作流结构正常" : "工作流结构基本正常（存在警告）", warnings);
    }

    private void dfsValidate(TreeNode node,
                             Set<TreeNode> visited,
                             Set<TreeNode> onPath,
                             boolean[] hasCycle,
                             boolean[] hasNullElement) {
        if (hasCycle[0]) {
            return;
        }
        if (node == null) {
            return;
        }
        if (node.getElement() == null) {
            hasNullElement[0] = true;
        }
        if (onPath.contains(node)) {
            hasCycle[0] = true;
            return;
        }
        if (visited.contains(node)) {
            return;
        }

        onPath.add(node);
        visited.add(node);
        for (TreeNode child : node.getChildren()) {
            dfsValidate(child, visited, onPath, hasCycle, hasNullElement);
            if (hasCycle[0]) {
                return;
            }
        }
        onPath.remove(node);
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
        prepareAggregationState();
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
            // 基于“聚合”语义：
            // 1) 记录当前父节点对各子节点的路由命中
            // 2) 将各子节点的 parentsLeft 计数减一
            // 3) 仅当 parentsLeft==0 且被至少一个父节点命中时，才调度执行该子节点

            List<TreeNode> allChildren = node.getChildren();
            List<TreeNode> allowedChildrenFromThisParent = node.getNextNodes(resultPool);
            Set<TreeNode> allowedSet = new HashSet<>(allowedChildrenFromThisParent);

            List<CompletableFuture<Void>> readyFutures = new ArrayList<>();
            // 无论是否执行都要减去一个left，因为这个初始的left是所有的子节点，无关她是否执行，如果该节点能执行则加入执行队列，不是则不加入
            for (TreeNode child : allChildren) {
                // 标记是否被本父节点放行
                if (allowedSet.contains(child)) {
                    allowedByAnyParent.get(child).set(true);
                }

                // 父计数 -1
                int left = parentsLeft.get(child).decrementAndGet();
                if (left == 0) {
                    // 全部父节点已完成，若至少一个父节点放行，则执行
                    if (allowedByAnyParent.get(child).get()) {
                        readyFutures.add(executeWorkflow(child));
                    } else {
                        log.info("skip child {}: no parent routed to it", child.getId());
                    }
                }
            }

            if (readyFutures.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.allOf(readyFutures.toArray(new CompletableFuture[0]));
        });
    }

    // 初始化聚合执行所需的 parentsLeft / allowedByAnyParent 状态
    private void prepareAggregationState() {
        TreeNode root = nodes.getRoot();
        this.parentsLeft = new ConcurrentHashMap<>();
        this.allowedByAnyParent = new ConcurrentHashMap<>();

        List<TreeNode> allNodes = collectAllNodes(root);
        for (TreeNode n : allNodes) {
            int parentCount = n.getParentNodes() == null ? 0 : n.getParentNodes().size();
            parentsLeft.put(n, new AtomicInteger(parentCount));
            allowedByAnyParent.put(n, new AtomicBoolean(false));
        }

        // 根节点：无父、可直接执行。其 allowed 与否不影响，它会被直接调度。
        parentsLeft.get(root).set(0);
    }

    private List<TreeNode> collectAllNodes(TreeNode root) {
        List<TreeNode> list = new ArrayList<>();
        if (root == null) {
            return list;
        }
        Queue<TreeNode> q = new ArrayDeque<>();
        Set<TreeNode> seen = new HashSet<>();
        q.add(root);
        seen.add(root);
        while (!q.isEmpty()) {
            TreeNode cur = q.poll();
            list.add(cur);
            for (TreeNode c : cur.getChildren()) {
                if (seen.add(c)) {
                    q.add(c);
                }
            }
        }
        return list;
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
