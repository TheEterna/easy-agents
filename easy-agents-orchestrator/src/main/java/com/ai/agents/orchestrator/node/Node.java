package com.ai.agents.orchestrator.node;

import com.ai.agents.orchestrator.workflow.*;
import com.ai.agents.orchestrator.workflow.WorkFlowManager.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;

/**
 * 节点是完全独立的, 不承担任何连接逻辑
 * @author han
 * @time 2025/7/28 10:52
 */

public abstract class Node<IN, OUT> {
    protected WorkFlowManager<?> workFlowManager;
    protected Class<OUT> outType;
    protected Class<IN> inType;
    protected IN input;

    protected Node() {

    }

    protected Node(IN input) {
        this();
        this.input = input;
        this.inType = (Class<IN>) input.getClass();
    }
    protected Node(UUID resultId) {
//        this();
        // 需要使用workflowManager对象获取到结果池中的 数据
        Objects.requireNonNull(resultId, "resultId不能为null");
        NodeResult nodeResult = workFlowManager.getResultPool().get(resultId);
        Objects.requireNonNull(nodeResult, "resultId不能为null");

        Object value = nodeResult.getValue();

        // 处理null值：null是任何引用类型的合法值，无需类型检查
//        if (value != null && !inType.isInstance(value)) {
//            // 异常信息包含目标类型、实际类型、节点id，便于调试
//            throw new IllegalArgumentException(String.format(
//                    "类型转换错误：目标类型=%s，实际类型=%s，节点Id=%s",
//                    inType.getName(),
//                    value.getClass().getName(),
//                    resultId
//            ));
//        }

//        // 安全转换类型（使用Class.cast()替代强制转换，消除警告）
//        this.input = inType.cast(value);
        this.input = (IN) value;
    }

    public abstract OUT executeNode();

//    public abstract OUT executeNode(IN input);


    public abstract static class NodeBuilder<IN, OUT, T extends Node<IN, OUT>> {
        protected WorkFlowManager<?> workFlowManager;
        protected Class<OUT> outType;
        protected Class<IN> inType;
        protected IN input;
        protected UUID resultId;

        // 设置工作流管理器
        public NodeBuilder<IN, OUT, T> workFlowManager(WorkFlowManager<?> workFlowManager) {
            this.workFlowManager = workFlowManager;
            return this;
        }

        // 设置输出类型
        public NodeBuilder<IN, OUT, T> outType(Class<OUT> outType) {
            this.outType = outType;
            return this;
        }

        // 设置输入类型
        public NodeBuilder<IN, OUT, T> inType(Class<IN> inType) {
            this.inType = inType;
            return this;
        }

        // 直接设置输入对象（与resultId互斥）
        public NodeBuilder<IN, OUT, T> input(IN input) {
            this.input = input;
            this.resultId = null; // 清除resultId，确保互斥
            return this;
        }

        // 通过resultId从结果池获取输入（与input互斥）
        public NodeBuilder<IN, OUT, T> resultId(UUID resultId) {
            this.resultId = resultId;
            this.input = null; // 清除input，确保互斥
            return this;
        }

        // 构建Node实例（抽象方法，由具体子类实现）
        public abstract T build(UUID resultId);
        // 构建Node实例（抽象方法，由具体子类实现）
        public abstract T build(IN input);

        // 验证参数合法性
        protected void validate() {
            // 当使用resultId时，必须设置workFlowManager
            if (resultId != null) {
                Objects.requireNonNull(workFlowManager, "使用resultId时必须设置workFlowManager");
            }
            // 验证输入类型（如果设置了input且inType不为空）
            if (input != null && inType != null && !inType.isInstance(input)) {
                throw new IllegalArgumentException(String.format(
                        "输入类型不匹配：期望%s，实际%s",
                        inType.getName(),
                        input.getClass().getName()
                ));
            }
        }
    }
}
