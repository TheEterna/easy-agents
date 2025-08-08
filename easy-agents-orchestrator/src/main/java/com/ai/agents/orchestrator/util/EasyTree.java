package com.ai.agents.orchestrator.util;

import com.ai.agents.orchestrator.node.CodeNode;
import com.ai.agents.orchestrator.node.Node;
import com.ai.agents.orchestrator.workflow.*;
import com.ai.agents.orchestrator.workflow.WorkFlowManager.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 基于普通多叉树的Set实现（非二叉、无自平衡）
 * 特性：元素唯一、支持迭代、保持树状层级结构
 * @author han
 * @time 2025/7/29 17:53
 */

public class EasyTree implements Iterable<Node> {

    /**
     * 多叉树节点
     */

    public static class TreeNode {
        private UUID id;
        private Node element;
        private TreeNode parent;
        // 在这里, 子节点上写一些 = xx 的路由选项, 用于路由, 并且可以 中间加&&, key 为 子节点
        private Map<TreeNode, RouteOption> childrenWithRouterOptions;

        public Node getElement() {
            return element;
        }

        public TreeNode getParent() {
            return parent;
        }

        public UUID getId() {
            return id;
        }

        public List<TreeNode> getChildren() {
            return childrenWithRouterOptions.keySet().stream().toList();
        }

        public List<TreeNode> getNextNodes(Map<UUID, NodeResult> resultPool) {
            // 如果没有路由选项，或者路由选项为空，则默认所有子节点都可以作为下一个节点。
            return childrenWithRouterOptions.entrySet().stream().filter((entry) -> {
                TreeNode childNode = entry.getKey();
                RouteOption routeOption = entry.getValue();
                if (childNode == null) {
                    return false;
                } else {
                    return routeOption == null || routeOption.evaluate(resultPool);
                }
            }).map(Map.Entry::getKey).collect(java.util.stream.Collectors.toList());

        }

        public TreeNode() {
            this.id = UUID.randomUUID();
        }
        public TreeNode(Node element) {
            this();
            this.element = element;
            this.childrenWithRouterOptions = new HashMap<>();
        }

        public TreeNode addChild(TreeNode child) {
            child.parent = this;
            // 直接添加到末尾，保持添加顺序
            childrenWithRouterOptions.put(child, null);
            return child;
        }

        public TreeNode addChild(Node child) {
            TreeNode childNode = new TreeNode(child);
            childNode.parent = this;
            // 直接添加到末尾，保持添加顺序
            childrenWithRouterOptions.put(childNode, null);
            return childNode;
        }

        // 移除子节点
        public boolean removeChild(TreeNode child) {
            return childrenWithRouterOptions.remove(child) != null;
        }

        // 查找子节点
        public TreeNode findChild(Node element) {
            for (TreeNode child : childrenWithRouterOptions.keySet()) {
                if (child.element.equals(element)) {
                    return child;
                }
            }
            return null;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TreeNode treeNode = (TreeNode) o;
            return Objects.equals(id, treeNode.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

    }

    private TreeNode root;
    private TreeNode indexNode;
    private int size;

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode setRoot(Node node) {
        this.root = new TreeNode(node);
        return this.root;
    }

    public EasyTree() {

        this.root = new TreeNode(CodeNode.<String>builder().code(input ->  {
            return "";
        }).build(""));
        this.size = 0;
        this.indexNode = root;
    }

    public EasyTree(Node rootElement) {
        this.root = new TreeNode(rootElement);
        this.size = 0;
        this.indexNode = root;
    }

    /**
     * 添加元素（保证唯一性，插入到合适的树节点下）
     * @param element 要添加的元素
     * @return 元素不存在且添加成功返回true
     */
    public TreeNode add(Node element) {
        if (element == null) {
            throw new NullPointerException("不支持null元素");
        }

        TreeNode insertTreeNode = new TreeNode(element);
        indexNode.addChild(element);
        size++;
        return insertTreeNode;
    }

    // 查找合适的父节点并插入新元素
    private void findParentAndInsert(Node element) {
        TreeNode current = root;
        while (true) {
            // 尝试查找是否已存在相同的子节点
            TreeNode child = current.findChild(element);

            if (child != null) {
                current = child; // 继续向下查找
            } else {
                // 找到合适的父节点，插入为子节点
                current.addChild(new TreeNode(element));
                break;
            }
        }
    }

    /**
     * 检查元素是否存在
     * @param o 要检查的元素
     * @return 存在返回true
     */
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        Node target = (Node) o;
        return findNode(target) != null;
    }

    // 查找元素对应的节点
    private TreeNode findNode(Node target) {
        if (root == null) {
            return null;
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            if (current.element != null && current.element.equals(target)) {
                return current;
            }
            // 将子节点加入队列（广度优先搜索）
            queue.addAll(current.getChildren());
        }
        return null;
    }

    /**
     * 移除元素
     * @param o 要移除的元素
     * @return 元素存在且移除成功返回true
     */
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        Node target = (Node) o;
        TreeNode node = findNode(target);

        if (node == null) {
            return false;
        }

        // 处理根节点的特殊情况
        if (node == root) {
            if (root.getChildren().isEmpty()) {
                root = null;
            } else {
                // 用第一个子节点作为新根，并合并其他子节点
                TreeNode newRoot = root.getChildren().remove(0);
                newRoot.getChildren().addAll(root.getChildren());
                newRoot.parent = null;
                root = newRoot;
            }
        } else {
            // 非根节点：将其所有子节点提升为父节点的子节点
            TreeNode parent = node.parent;
            parent.removeChild(node);
            // 合并子节点到父节点
            for (TreeNode child : node.getChildren()) {
                parent.addChild(child);
            }
        }

        size--;
        return true;
    }

    /**
     * 迭代器（前序遍历：根->子节点）
     */
    public Iterator<Node> iterator() {
        List<Node> elements = new ArrayList<>(size);
        preOrderTraversal(root, elements);
        return elements.iterator();
    }

    // 前序遍历收集元素
    private void preOrderTraversal(TreeNode node, List<Node> elements) {
        if (node == null) {
            return;
        }
        if (node.element != null) {
            elements.add(node.element);
        }
        // 按顺序遍历子节点
        for (TreeNode child : node.getChildren()) {
            preOrderTraversal(child, elements);
        }
    }

    /**
     * 批量添加元素
     */
    public boolean addAll(Collection<? extends Node> c) {
        boolean modified = false;
        for (Node t : c) {
            if (add(t) != null) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 清空集合
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * 检查是否包含所有元素
     */
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 移除所有包含的元素
     */
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            if (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 保留集合中与参数共有的元素
     */
    public boolean retainAll(Collection<?> c) {
        Set<?> collectionSet = new HashSet<>(c);
        List<Node> toRemove = new ArrayList<>();
        for (Node t : this) {
            if (!collectionSet.contains(t)) {
                toRemove.add(t);
            }
        }
        return removeAll(toRemove);
    }

    /**
     * 获取集合大小
     */
    public int size() {
        return size;
    }

    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 转换为数组
     */
    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    /**
     * 转换为指定类型数组
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        for (Node t : this) {
            a[i++] = (T) t;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    /**
     * 打印树结构（用于调试）
     */
    public void printTree() {
        if (root == null) {
            System.out.println("Empty tree");
            return;
        }
        printTreeNode(root, 0);
    }

    private void printTreeNode(TreeNode node, int depth) {
        // 打印当前节点（根据深度缩进）
        if (node.element != null) {
            System.out.println("  ".repeat(depth) + "├─ " + node.element);
        } else {
            System.out.println("  ".repeat(depth) + "├─ " + "ROOT");
        }
        // 递归打印子节点
        for (TreeNode child : node.getChildren()) {
            printTreeNode(child, depth + 1);
        }
    }

}
