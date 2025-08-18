package com.ai.agents.orchestrator.util;

import com.ai.agents.orchestrator.util.NodeResult;
import com.ai.agents.orchestrator.util.EasyTree.TreeNode;

import java.util.*;
import java.util.function.Predicate;

/**
 * RouteOption 定义了从一个节点到另一个节点的路由条件。
 * 它由一个或多个条件（Predicate）和一个目标节点（TreeNode）组成。
 * 使用构建器模式（Builder）可以优雅地创建复杂的路由逻辑。
 *
 * 示例:
 * <pre>{@code
 * RouteOption option = RouteOption
 *     .when(resultPool -> ((Integer) resultPool.get(nodeA).getValue()) > 10)
 *     .and(resultPool -> "OK".equals(resultPool.get(nodeB).getValue()))
 *     .then(nextNode);
 * }</pre>
 *
 * @author han
 * @time 2025/7/29 17:12
 */
public class RouteOption {

    private final Predicate<Map<UUID, NodeResult>> condition;

    private RouteOption(Predicate<Map<UUID, NodeResult>> condition) {
        this.condition = condition;
    }

    /**
     * 评估路由条件。
     *
     * @param resultPool 工作流的结果池，用于条件判断。
     * @return 如果条件满足，则返回 true。
     */
    public boolean evaluate(Map<UUID, NodeResult> resultPool) {
        return condition.test(resultPool);
    }



    /**
     * 启动构建器，创建第一个路由条件。
     *
     * @param condition 初始条件。
     * @return 一个新的构建器实例。
     */
    public static Builder when(Predicate<Map<UUID, NodeResult>> condition) {
        return new Builder(condition);
    }

    /**
     * 构建器类，用于链式创建 RouteOption。
     */
    public static class Builder {
        private Predicate<Map<UUID, NodeResult>> combinedCondition;

        private Builder(Predicate<Map<UUID, NodeResult>> condition) {
            this.combinedCondition = condition;
        }

        /**
         * 使用 "AND" 逻辑连接下一个条件。
         *
         * @param nextCondition 下一个条件。
         * @return 当前构建器实例。
         */
        public Builder and(Predicate<Map<UUID, NodeResult>> nextCondition) {
            this.combinedCondition = this.combinedCondition.and(nextCondition);
            return this;
        }

        /**
         * 使用 "OR" 逻辑连接下一个条件。
         *
         * @param nextCondition 下一个条件。
         * @return 当前构建器实例。
         */
        public Builder or(Predicate<Map<UUID, NodeResult>> nextCondition) {
            this.combinedCondition = this.combinedCondition.or(nextCondition);
            return this;
        }

        public RouteOption build() {
            return new RouteOption(this.combinedCondition);
        }
    }
}

