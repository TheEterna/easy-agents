
![](https://theeterna.github.io/easy-agents-docs/logo.svg)
# Easy-Agents - 轻量级 AI Agents 工作流编排

## 项目简介

Easy-Agents 是一个基于 Java 17 的轻量级工作流编排框架，用于拼装 AI/非 AI 节点并行/串行执行，提供阻塞与流式两种执行模式，以及可组合的路由与并发控制。核心不依赖 Spring Boot，可在任意 Java 应用中嵌入使用；如需大模型对话，可通过 `AIChatNode` 对接 Spring AI。

## 项目特性

- ✨ **多智能体协作**: 支持多个智能体的并行和串行执行
- 🔄 **工作流编排**: 灵活的工作流定义和管理
- 🌳 **树形结构**: 基于树形结构的工作流节点管理
- 🎯 **智能路由**: 支持条件路由和动态决策
- 📕 **模块化设计**: 清晰的模块分离和依赖管理
- 🌟 **高性能**: 基于 Spring Boot 的高性能架构

## 技术栈

- Java 17+
- Reactor（Flux）用于流式事件
- Maven 多模块
- 可选：Spring AI（用于 `AIChatNode` 对话能力）

## 项目结构
```
easy-agents/
├── easy-agents-common/
│   └── src/main/java/com/ai/agents/common/model/ValidationResult.java
├── easy-agents-orchestrator/
│   └── src/main/java/com/ai/agents/orchestrator/
│       ├── node/
│       │   ├── Node.java
│       │   ├── AIChatNode.java
│       │   └── CodeNode.java
│       ├── util/
│       │   ├── EasyTree.java
│       │   └── RouteOption.java
│       └── workflow/WorkFlowManager.java
└── pom.xml
```

## 核心组件

### 1. Node（节点）
- `Node<IN>` 抽象基类，支持阻塞与流式两种执行通道。
- `CodeNode<IN>`：以 Lambda 实现自定义处理逻辑。
- `AIChatNode<IN>`：集成 Spring AI，支持对话与流式输出。

### 2. EasyTree (树形结构)
- 支持多父节点的树形结构
- 灵活的子节点管理
- 路由选项配置

### 3. WorkFlowManager（工作流管理器）
- `setStartNode(root)` 设置起始节点（仅一次）。
- `startBlocking()` 返回聚合结果池 `Map<UUID, NodeResult>`.
- `startStreaming()` 返回事件流 `Flux<Object>`,边执行边发射。
- 内部基于 `parentsLeft` 与 `allowedByAnyParent` 控制多父阻塞与放行。

### 4. ValidationResult（校验结果）
- 工作流构建/执行的状态与提示封装。

## 快速开始

### 环境
- JDK 17+
- Maven 3.8+

### 获取与构建
```bash
git clone <repository-url>
cd easy-agents
mvn -q -DskipTests package
```

## 使用示例

### 创建最小工作流（流式）
```java
import com.ai.agents.orchestrator.workflow.WorkFlowManager;
import com.ai.agents.orchestrator.node.CodeNode;
import reactor.core.publisher.Flux;

WorkFlowManager<String> manager = WorkFlowManager.<String>builder().build();

CodeNode<String> start = CodeNode.<String>builder()
    .inType(String.class)
    .outType(String.class)
    .code(in -> "Hello " + in)
    .build("World");

manager.setStartNode(start); // 仅设置一次

Flux<Object> flux = manager.startStreaming()
    .doOnNext(System.out::println)
    .doOnError(Throwable::printStackTrace)
    .doOnComplete(() -> System.out.println("DONE"));

flux.blockLast();
```

### 条件路由（RouteOption）
```java
import com.ai.agents.orchestrator.util.EasyTree;
import com.ai.agents.orchestrator.util.RouteOption;

EasyTree.TreeNode root = manager.setStartNode(start);

RouteOption route = RouteOption
    .when(pool -> ((String) pool.get(root.getId()).getValue()).length() > 5)
    .and(pool -> true) // 可继续追加 and/or 条件
    .build();

root.addChild(CodeNode.<String>builder()
        .outType(String.class)
        .code(s -> s + " !")
        .build(root.getId()),
    route);
```

## 开发指南

### 添加新的节点类型
1. 继承 `Node<T>` 抽象类
2. 实现 `execute()` 方法
3. 在 `WorkFlowManager` 中注册

### 扩展工作流功能
1. 继承 `WorkFlowManager`
2. 添加新的管理方法
3. 实现自定义的执行逻辑

## 配置说明
- 核心库零配置即可使用；如需 `AIChatNode`, 请按 Spring AI 官方文档配置模型提供方（API Key 等）。

## 测试

运行测试用例：
```bash
mvn test
```

运行特定测试：
```bash
mvn test -Dtest=WorkFlowTests
```

## 文档与示例
- 文档站（VitePress）：`easy-agents-docs/`（单独仓库/目录）。
  - 本地：`npm i && npm run docs:dev`
  - 章节：架构概览、工作流与节点、流式执行、路由与并发、测试与调试、示例等。

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 作者: han
- 项目链接: https://github.com/TheEterna/easy-agents

## 更新日志

### v0.0.1-SNAPSHOT
- 新增 `easy-agents-common` 公共模块
- 实现 `ValidationResult` 工作流校验结果类
- 重构 `EasyTree` 支持多父节点结构
- 更新项目依赖配置

---

**注意**: 本项目仍在开发中，API 可能会有变化。建议在生产环境使用前进行充分测试。