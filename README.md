# Easy Agents - 多智能体编排框架

## 项目简介
Easy Agents是一个简单易用的多智能体编排框架，旨在帮助开发者快速构建和管理多个AI智能体之间的协作。

## 项目结构
```
easy-agents/
├── easy-agents-orchestrator/     # 编排器模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ai/agents/
│   │   │   │       ├── Agent.java
│   │   │   │       ├── SimpleAgent.java
│   │   │   │       └── orchestrator/
│   │   │   │           ├── Application.java
│   │   │   │           ├── Orchestrator.java
│   │   │   │           └── AgentConfiguration.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
└── pom.xml
```

## 技术栈
- Java 17
- Spring Boot 3.4.4
- Maven

## 核心组件

### Agent (智能体接口)
定义了智能体的基本行为，包括执行任务、获取ID和能力描述等方法。

### Orchestrator (编排器)
负责管理智能体的注册、任务分配和协调工作，提供以下功能：
- 注册和管理智能体
- 执行单个智能体任务
- 广播任务给所有智能体

### SimpleAgent (简单智能体实现)
提供了一个简单的智能体实现示例，展示如何实现Agent接口。

## 快速开始
1. 克隆项目
2. 使用Maven构建项目：
   ```
   mvn clean install
   ```
3. 运行应用：
   ```
   mvn spring-boot:run
   ```

## 使用示例
项目启动后会自动注册三个示例智能体：
- greeting-agent: 处理问候任务
- calculator-agent: 执行数学计算
- general-agent: 处理通用任务

## 开发指南
1. 创建新的智能体类，实现Agent接口
2. 在AgentConfiguration中注册新的智能体
3. 通过Orchestrator执行任务
4. 测试智能体协作

## 扩展建议
1. 添加智能体间通信机制
2. 实现任务调度和优先级管理
3. 添加智能体状态监控
4. 支持动态注册和注销智能体
5. 实现智能体负载均衡

## 贡献指南
欢迎提交Issue和Pull Request来改进这个项目。
