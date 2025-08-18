package com.ai.agents.orchestrator.node;

import com.ai.agents.orchestrator.util.*;
import com.ai.agents.orchestrator.workflow.*;
import com.ai.agents.orchestrator.workflow.WorkFlowManager.*;
import com.ai.agents.common.model.*;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 节点是完全独立的, 不承担任何连接逻辑
 * @author han
 * @time 2025/7/28 10:52
 */

public abstract class Node<IN> {
    protected WorkFlowManager<?> workFlowManager;
//    protected Class<OUT> outType;
    protected Class<IN> inType;
    protected IN input;
    protected Class<?> outType;

    protected UUID inputResultId;

    protected Node() {

    }

    protected Node(IN input) {
        this();
        this.input = input;
        // 如果输入不为null，优先使用输入的实际类型
        if (input != null) {
            this.inType = (Class<IN>) input.getClass();
        }
    }

    public void setWorkFlowManager(WorkFlowManager<?> workFlowManager) {
        this.workFlowManager = workFlowManager;
    }

    protected Node(UUID inputResultId) {
        // 需要使用workflowManager对象获取到结果池中的 数据
        Objects.requireNonNull(inputResultId, "inputResultId不能为null");
        this.inputResultId = inputResultId;

    }

    public <OUT> OUT executeNodeBlocking() {
        if (inputResultId != null) {
            NodeResult nodeResult = workFlowManager.getResultPool().get(inputResultId);
            Object value = nodeResult.getValue();
            this.input = (IN) value;
        }
        return executeBlocking();
    }
    public abstract <OUT> OUT executeBlocking();


    public Flux<?> executeNodeStreaming() {
        if (inputResultId != null) {
            NodeResult nodeResult = workFlowManager.getResultPool().get(inputResultId);
            Object value = nodeResult.getValue();
            this.input = (IN) value;
        }
        return executeStreaming();
    }

    public abstract Flux<?> executeStreaming();


    public abstract static class NodeBuilder<IN, B ,T extends Node<IN>> {

        protected WorkFlowManager<?> workFlowManager;
        protected Class<IN> inType;
        protected Class<?> outType;
        protected IN input;
        protected UUID inputResultId;

        // 设置工作流管理器
        public B workFlowManager(WorkFlowManager<?> workFlowManager) {
            this.workFlowManager = workFlowManager;
            return (B) this;
        }

        // 设置输入类型
        public B inType(Class<IN> inType) {
            this.inType = inType;
            return (B) this;
        }

        // 设置输入类型
        public B outType(Class<?> outType) {
            this.outType = outType;
            return (B) this;
        }
        // 直接设置输入对象（与inputResultId互斥）
        public B input(IN input) {
            this.input = input;
            this.inputResultId = null; // 清除inputResultId，确保互斥
            return (B) this;
        }

        // 通过inputResultId从结果池获取输入（与input互斥）
        public B inputResultId(UUID inputResultId) {
            this.inputResultId = inputResultId;
            this.input = null; // 清除input，确保互斥
            return (B) this;
        }

        // 构建Node实例（抽象方法，由具体子类实现）
        public abstract T build(UUID inputResultId);
        // 构建Node实例（抽象方法，由具体子类实现）
        public abstract T build(IN input);

        // 验证参数合法性
        protected void validate() {
            // 当使用inputResultId时，必须设置workFlowManager
            if (inputResultId != null) {
                Objects.requireNonNull(workFlowManager, "使用inputResultId时必须设置workFlowManager");
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
