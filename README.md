# Easy Agents - 多智能体编排框架

## 项目简介

Easy Agents 是一个基于 Java 17 和 Spring Boot 的多智能体编排框架，旨在帮助开发者快速构建和管理多个 AI 智能体之间的协作。该框架提供了灵活的工作流编排、智能体节点管理和路由控制功能。

## 项目特性

- �� **多智能体协作**: 支持多个智能体的并行和串行执行
- 🔄 **工作流编排**: 灵活的工作流定义和管理
- 🌳 **树形结构**: 基于树形结构的工作流节点管理
- ��️ **智能路由**: 支持条件路由和动态决策
- �� **模块化设计**: 清晰的模块分离和依赖管理
- ⚡ **高性能**: 基于 Spring Boot 的高性能架构

## 技术栈

- **Java**: 17+
- **Spring Boot**: 3.4.4
- **Maven**: 多模块项目管理
- **IDE**: IntelliJ IDEA 支持

## 项目结构
```
easy-agents/
├── easy-agents-common/ # 公共模块
│ ├── src/main/java/
│ │ └── com/ai/agents/common/
│ │ └── model/
│ │ └── ValidationResult.java # 工作流校验结果
│ └── pom.xml
├── easy-agents-orchestrator/ # 编排器核心模块
│ ├── src/main/java/
│ │ └── com/ai/agents/orchestrator/
│ │ ├── Main.java # 主程序入口
│ │ ├── node/ # 节点类型定义
│ │ │ ├── Node.java # 基础节点抽象类
│ │ │ ├── AIChatNode.java # AI 聊天节点
│ │ │ └── CodeNode.java # 代码执行节点
│ │ ├── util/ # 工具类
│ │ │ ├── EasyTree.java # 树形结构管理
│ │ │ └── RouteOption.java # 路由选项
│ │ └── workflow/ # 工作流管理
│ │ └── WorkFlowManager.java # 工作流管理器
│ ├── src/main/resources/
│ │ └── application.yml # 应用配置
│ └── pom.xml
└── pom.xml # 父级 POM
```


## 核心组件

### 1. Node (节点)
- **基础节点**: 所有智能体节点的抽象基类
- **AI 聊天节点**: 处理 AI 对话和交互
- **代码执行节点**: 执行自定义代码逻辑

### 2. EasyTree (树形结构)
- 支持多父节点的树形结构
- 灵活的子节点管理
- 路由选项配置

### 3. WorkFlowManager (工作流管理器)
- 工作流的创建、执行和监控
- 节点间的数据传递
- 执行结果管理

### 4. ValidationResult (校验结果)
- 工作流执行状态校验
- 警告信息收集
- 执行结果反馈

## 快速开始

### 环境要求
- JDK 17 或更高版本
- Maven 3.6 或更高版本
- IntelliJ IDEA (推荐)

### 1. 克隆项目
```bash
git clone <repository-url>
cd easy-agents
```

### 2. 构建项目
```bash
mvn clean install
```

### 3. 运行应用
```bash
cd easy-agents-orchestrator
mvn spring-boot:run
```

## 使用示例

### 创建简单工作流
```java
// 创建工作流管理器
WorkFlowManager manager = WorkFlowManager.builder()
    .name("示例工作流")
    .build();

// 添加节点
AIChatNode chatNode = AIChatNode.builder()
    .name("AI 聊天节点")
    .build();

CodeNode<String> codeNode = CodeNode.<String>builder()
    .name("代码执行节点")
    .code(input -> "处理结果: " + input)
    .build();

// 构建工作流
manager.addNode(chatNode);
manager.addNode(codeNode);
```

### 配置路由选项
```java
RouteOption routeOption = RouteOption.builder()
    .condition("input.length() > 10")
    .build();

// 添加带路由的子节点
treeNode.addChild(childNode, routeOption);
```

## 开发指南

### 添加新的节点类型
1. 继承 `Node<T>` 抽象类
2. 实现 `execute()` 方法
3. 在 `WorkFlowManager` 中注册

### 自定义路由逻辑
1. 实现 `RouteOption` 接口
2. 定义路由条件
3. 在树形结构中应用

### 扩展工作流功能
1. 继承 `WorkFlowManager`
2. 添加新的管理方法
3. 实现自定义的执行逻辑

## 配置说明

### application.yml
```yaml
spring:
  application:
    name: easy-agents-orchestrator
  
server:
  port: 8080

# 自定义配置
easy-agents:
  workflow:
    max-nodes: 1000
    timeout: 30000
```

## 测试

运行测试用例：
```bash
mvn test
```

运行特定测试：
```bash
mvn test -Dtest=WorkFlowTests
```

## 部署

### 打包
```bash
mvn clean package
```

### 运行 JAR 文件
```bash
java -jar easy-agents-orchestrator/target/easy-agents-orchestrator-0.0.0-SNAPSHOT.jar
```

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
- 项目链接: [https://github.com/yourusername/easy-agents](https://github.com/yourusername/easy-agents)

## 更新日志

### v0.0.1-SNAPSHOT
- 新增 `easy-agents-common` 公共模块
- 实现 `ValidationResult` 工作流校验结果类
- 重构 `EasyTree` 支持多父节点结构
- 优化工作流节点管理
- 更新项目依赖配置

---

**注意**: 本项目仍在开发中，API 可能会有变化。建议在生产环境使用前进行充分测试。