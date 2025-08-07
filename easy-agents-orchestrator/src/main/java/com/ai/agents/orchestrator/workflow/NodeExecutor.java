package com.ai.agents.orchestrator.workflow;

public class NodeExecutor {
    // 成员变量暂存泛型类型
    private Class<?> outType;

    // 第一个方法：配置泛型类型OUT
    public <OUT> void configureOutputType(Class<OUT> outType) {
        this.outType = outType;
    }

    // 第二个方法：无参执行，使用已配置的泛型类型
    @SuppressWarnings("unchecked")
    public <OUT> OUT executeNode() {
        if (outType == null) {
            throw new IllegalStateException("请先调用configureOutputType配置输出类型");
        }

        // 这里是原executeNode的核心逻辑，示例中用反射创建实例演示
        try {
            // 通过已配置的outType创建实例（实际逻辑根据业务调整）
            return (OUT) outType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("执行节点失败", e);
        }
    }

    // 使用示例
    public static void main(String[] args) {
        NodeExecutor executor = new NodeExecutor();

        // 1. 先配置输出类型为String
        executor.configureOutputType(String.class);
        // 2. 调用无参executeNode获取结果
        String strResult = executor.executeNode();
        System.out.println("String结果类型：" + strResult.getClass());

        // 重新配置为Integer
        executor.configureOutputType(Integer.class);
        Integer intResult = executor.executeNode();
        System.out.println("Integer结果类型：" + intResult.getClass());
    }
}
