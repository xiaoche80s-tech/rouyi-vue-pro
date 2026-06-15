# WebSocket实时通信服务

<cite>
**本文档引用的文件**
- [WebSocketProperties.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/WebSocketProperties.java)
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)
- [WebSocketMessageListener.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/listener/WebSocketMessageListener.java)
- [JsonWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/message/JsonWebSocketMessage.java)
- [WebSocketSessionManager.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManager.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [WebSocketSessionHandlerDecorator.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionHandlerDecorator.java)
- [WebSocketAuthorizeRequestsCustomizer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/WebSocketAuthorizeRequestsCustomizer.java)
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)
- [LocalWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/local/LocalWebSocketMessageSender.java)
- [RedisWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessage.java)
- [RedisWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageConsumer.java)
- [RedisWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageSender.java)
- [KafkaWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessage.java)
- [KafkaWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageConsumer.java)
- [KafkaWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageSender.java)
- [RocketMQWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessage.java)
- [RocketMQWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageConsumer.java)
- [RocketMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageSender.java)
- [RabbitMQWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessage.java)
- [RabbitMQWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageConsumer.java)
- [RabbitMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageSender.java)
- [WebSocketSenderApi.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApi.java)
- [WebSocketSenderApiImpl.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApiImpl.java)
</cite>

## 目录
1. [引言](#引言)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构总览](#架构总览)
5. [详细组件分析](#详细组件分析)
6. [依赖关系分析](#依赖关系分析)
7. [性能考虑](#性能考虑)
8. [故障排查指南](#故障排查指南)
9. [结论](#结论)
10. [附录](#附录)

## 引言
本文件面向WebSocket实时通信服务的技术文档，系统性阐述连接管理机制（连接建立、心跳检测、断线重连、连接池管理）、消息路由算法（用户标识、会话管理、消息分发策略）、配置参数（超时、缓冲区、并发限制）、连接状态监控与异常处理、性能优化策略（连接复用、消息压缩、批量处理），以及安全防护（认证、消息加密、访问控制）。文档基于仓库中的实际实现进行分析，确保读者能够准确理解系统的架构与运行机制。

## 项目结构
WebSocket相关能力主要集中在框架模块与基础设施模块中：
- 框架层提供WebSocket自动装配、消息编解码、会话管理、消息发送器抽象与多后端实现（本地、Redis、Kafka、RocketMQ、RabbitMQ）、安全拦截与鉴权定制等。
- 基础设施模块提供对外的发送器API接口与实现，便于业务模块调用。

```mermaid
graph TB
subgraph "框架层(yudao-spring-boot-starter-websocket)"
A["配置<br/>WebSocketProperties"] --> B["自动装配<br/>YudaoWebSocketAutoConfiguration"]
C["消息处理器<br/>JsonWebSocketMessageHandler"] --> D["监听器<br/>WebSocketMessageListener"]
E["会话管理<br/>WebSocketSessionManagerImpl"] --> F["会话装饰器<br/>WebSocketSessionHandlerDecorator"]
G["消息发送器抽象<br/>AbstractWebSocketMessageSender"] --> H["本地发送器<br/>LocalWebSocketMessageSender"]
G --> I["Redis发送器<br/>RedisWebSocketMessageSender"]
G --> J["Kafka发送器<br/>KafkaWebSocketMessageSender"]
G --> K["RocketMQ发送器<br/>RocketMQWebSocketMessageSender"]
G --> L["RabbitMQ发送器<br/>RabbitMQWebSocketMessageSender"]
M["安全拦截与鉴权<br/>LoginUserHandshakeInterceptor / WebSocketAuthorizeRequestsCustomizer"]
end
subgraph "基础设施模块(yudao-module-infra)"
N["发送器API接口<br/>WebSocketSenderApi"] --> O["发送器API实现<br/>WebSocketSenderApiImpl"]
end
B --> C
B --> E
B --> G
B --> M
O --> G
```

图表来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)
- [LocalWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/local/LocalWebSocketMessageSender.java)
- [RedisWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageSender.java)
- [KafkaWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageSender.java)
- [RocketMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageSender.java)
- [RabbitMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageSender.java)
- [WebSocketSenderApi.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApi.java)
- [WebSocketSenderApiImpl.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApiImpl.java)

章节来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [WebSocketProperties.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/WebSocketProperties.java)

## 核心组件
- 配置中心：定义WebSocket连接路径与消息发送器类型等配置项。
- 自动装配：负责注册消息处理器、会话管理器、消息发送器、安全拦截与鉴权定制等。
- 消息编解码：以JSON格式的消息体进行收发，支持类型与内容分离。
- 会话管理：维护用户类型、用户ID、Session ID之间的映射关系，支持按用户或按Session分发。
- 消息发送器：抽象统一的发送接口，提供本地与多种中间件（Redis、Kafka、RocketMQ、RabbitMQ）实现。
- 安全控制：握手阶段进行登录用户绑定与请求授权定制。

章节来源
- [WebSocketProperties.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/WebSocketProperties.java)
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)
- [JsonWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/message/JsonWebSocketMessage.java)
- [WebSocketSessionManager.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManager.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)
- [WebSocketAuthorizeRequestsCustomizer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/WebSocketAuthorizeRequestsCustomizer.java)
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)

## 架构总览
WebSocket整体架构围绕“自动装配—消息编解码—会话管理—消息发送—安全控制”展开，支持本地直发与分布式广播两种模式，并通过配置项选择发送器类型。

```mermaid
graph TB
Client["客户端"] --> WS["WebSocket服务器"]
WS --> Handler["消息处理器<br/>JsonWebSocketMessageHandler"]
Handler --> Listener["消息监听器<br/>WebSocketMessageListener"]
WS --> SM["会话管理器<br/>WebSocketSessionManagerImpl"]
WS --> Sender["消息发送器<br/>AbstractWebSocketMessageSender"]
Sender --> Local["本地发送器<br/>LocalWebSocketMessageSender"]
Sender --> Redis["Redis发送器<br/>RedisWebSocketMessageSender"]
Sender --> Kafka["Kafka发送器<br/>KafkaWebSocketMessageSender"]
Sender --> RMQ["RocketMQ发送器<br/>RocketMQWebSocketMessageSender"]
Sender --> Rabbit["RabbitMQ发送器<br/>RabbitMQWebSocketMessageSender"]
WS --> Sec["安全拦截与鉴权<br/>LoginUserHandshakeInterceptor / WebSocketAuthorizeRequestsCustomizer"]
API["发送器API实现<br/>WebSocketSenderApiImpl"] --> Sender
```

图表来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)
- [WebSocketMessageListener.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/listener/WebSocketMessageListener.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)
- [LocalWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/local/LocalWebSocketMessageSender.java)
- [RedisWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageSender.java)
- [KafkaWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageSender.java)
- [RocketMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageSender.java)
- [RabbitMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageSender.java)
- [WebSocketSenderApiImpl.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApiImpl.java)

## 详细组件分析

### 连接管理机制
- 连接建立：通过自动装配注册的WebSocket处理器与会话管理器完成握手与会话初始化，登录用户信息在握手阶段注入。
- 心跳检测：当前仓库未直接暴露心跳配置与实现细节；建议结合Spring WebSocket的SockJS与STOMP配置补充心跳参数（例如超时、缓冲区、并发限制）。
- 断线重连：当前仓库未提供断线重连的具体实现；可在客户端侧实现指数退避与最大重试次数策略，并在服务端侧保留会话上下文以便恢复。
- 连接池管理：当前仓库未提供连接池实现；可结合Netty或Tomcat的WebSocket容器参数进行连接数与并发限制配置。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant Interceptor as "握手拦截器<br/>LoginUserHandshakeInterceptor"
participant Manager as "会话管理器<br/>WebSocketSessionManagerImpl"
participant Handler as "消息处理器<br/>JsonWebSocketMessageHandler"
Client->>Interceptor : "发起WebSocket握手"
Interceptor->>Interceptor : "校验登录用户"
Interceptor-->>Client : "返回握手结果"
Client->>Manager : "建立会话并登记用户类型/用户ID/Session ID"
Client->>Handler : "发送消息"
Handler-->>Client : "响应消息"
```

图表来源
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)

章节来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)

### 消息路由算法
- 用户标识：消息体包含用户类型与用户ID，用于按用户维度路由。
- 会话管理：会话管理器维护用户类型/用户ID到Session ID的映射，支持按用户或按Session分发。
- 消息分发策略：根据配置选择发送器类型，本地直发或通过中间件广播至其他节点。

```mermaid
flowchart TD
Start(["接收消息"]) --> Parse["解析消息体<br/>用户类型/用户ID/消息类型/内容"]
Parse --> Route{"目标是按用户还是按Session?"}
Route --> |按用户| Lookup["查询会话管理器<br/>用户类型+用户ID -> Session ID"]
Route --> |按Session| UseSID["直接使用Session ID"]
Lookup --> Send["调用消息发送器<br/>选择发送器类型"]
UseSID --> Send
Send --> End(["完成分发"])
```

图表来源
- [JsonWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/message/JsonWebSocketMessage.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)

章节来源
- [JsonWebSocketMessage.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/message/JsonWebSocketMessage.java)
- [WebSocketSessionManager.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManager.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)

### 配置参数
- 连接路径：可通过配置项设置WebSocket连接路径，默认为/ws。
- 发送器类型：可选local、redis、rocketmq、kafka、rabbitmq，用于决定消息分发方式。

章节来源
- [WebSocketProperties.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/WebSocketProperties.java)

### 连接状态监控与异常处理
- 监控：当前仓库未提供专门的连接状态监控实现；建议结合Actuator与自定义指标收集连接数、消息吞吐量、错误率等。
- 异常处理：当前仓库未提供专门的异常处理实现；建议在消息处理器与发送器中增加try-catch与降级策略，记录异常日志并返回友好提示。

### 性能优化策略
- 连接复用：建议客户端使用长连接与连接池，减少握手开销。
- 消息压缩：对大消息采用压缩传输，降低带宽占用。
- 批量处理：聚合多条小消息为批次发送，提升吞吐量。
- 发送器选择：在单机场景使用local，在集群场景使用Redis/Kafka/RocketMQ/RabbitMQ广播，平衡延迟与一致性。

### 安全防护措施
- 连接认证：握手阶段绑定登录用户，拒绝未认证请求。
- 访问控制：通过鉴权定制器限制特定URL或用户角色的访问。
- 消息加密：建议在应用层对敏感消息进行加解密处理（当前仓库未提供具体实现）。

章节来源
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)
- [WebSocketAuthorizeRequestsCustomizer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/WebSocketAuthorizeRequestsCustomizer.java)

## 依赖关系分析
- 自动装配依赖于消息处理器、会话管理器、消息发送器与安全组件。
- 消息发送器依赖于会话管理器进行用户到会话的映射。
- 不同发送器实现共享抽象基类，保证统一接口与扩展性。
- 发送器API实现依赖于消息发送器，供业务模块调用。

```mermaid
classDiagram
class YudaoWebSocketAutoConfiguration
class JsonWebSocketMessageHandler
class WebSocketMessageListener
class WebSocketSessionManager
class WebSocketSessionManagerImpl
class WebSocketMessageSender
class AbstractWebSocketMessageSender
class LocalWebSocketMessageSender
class RedisWebSocketMessageSender
class KafkaWebSocketMessageSender
class RocketMQWebSocketMessageSender
class RabbitMQWebSocketMessageSender
class LoginUserHandshakeInterceptor
class WebSocketAuthorizeRequestsCustomizer
class WebSocketSenderApiImpl
YudaoWebSocketAutoConfiguration --> JsonWebSocketMessageHandler
YudaoWebSocketAutoConfiguration --> WebSocketSessionManagerImpl
YudaoWebSocketAutoConfiguration --> WebSocketMessageSender
YudaoWebSocketAutoConfiguration --> LoginUserHandshakeInterceptor
YudaoWebSocketAutoConfiguration --> WebSocketAuthorizeRequestsCustomizer
WebSocketMessageSender <|-- AbstractWebSocketMessageSender
AbstractWebSocketMessageSender <|-- LocalWebSocketMessageSender
AbstractWebSocketMessageSender <|-- RedisWebSocketMessageSender
AbstractWebSocketMessageSender <|-- KafkaWebSocketMessageSender
AbstractWebSocketMessageSender <|-- RocketMQWebSocketMessageSender
AbstractWebSocketMessageSender <|-- RabbitMQWebSocketMessageSender
WebSocketSenderApiImpl --> WebSocketMessageSender
```

图表来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [JsonWebSocketMessageHandler.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/handler/JsonWebSocketMessageHandler.java)
- [WebSocketMessageListener.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/listener/WebSocketMessageListener.java)
- [WebSocketSessionManager.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManager.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)
- [LocalWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/local/LocalWebSocketMessageSender.java)
- [RedisWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageSender.java)
- [KafkaWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageSender.java)
- [RocketMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageSender.java)
- [RabbitMQWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageSender.java)
- [WebSocketSenderApiImpl.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApiImpl.java)

章节来源
- [YudaoWebSocketAutoConfiguration.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/config/YudaoWebSocketAutoConfiguration.java)
- [WebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/WebSocketMessageSender.java)
- [AbstractWebSocketMessageSender.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/AbstractWebSocketMessageSender.java)

## 性能考虑
- 发送器类型选择：单机使用local，集群使用分布式广播，权衡延迟与一致性。
- 消息体设计：采用紧凑的JSON结构，避免冗余字段。
- 批量与压缩：对高频小消息进行批量与压缩，减少网络开销。
- 并发与限流：结合容器参数与业务限流策略，防止过载。

## 故障排查指南
- 握手失败：检查登录拦截器是否正确绑定用户，确认鉴权定制器规则。
- 消息未送达：核对会话管理器中是否存在对应用户/会话映射，检查发送器类型与中间件配置。
- 分布式广播异常：检查Redis/Kafka/RocketMQ/RabbitMQ的连接与订阅通道。

章节来源
- [LoginUserHandshakeInterceptor.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/security/LoginUserHandshakeInterceptor.java)
- [WebSocketSessionManagerImpl.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/session/WebSocketSessionManagerImpl.java)
- [RedisWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/redis/RedisWebSocketMessageConsumer.java)
- [KafkaWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/kafka/KafkaWebSocketMessageConsumer.java)
- [RocketMQWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rocketmq/RocketMQWebSocketMessageConsumer.java)
- [RabbitMQWebSocketMessageConsumer.java](file://yudao-framework/yudao-spring-boot-starter-websocket/src/main/java/cn/iocoder/yudao/framework/websocket/core/sender/rabbitmq/RabbitMQWebSocketMessageConsumer.java)

## 结论
该WebSocket服务通过自动装配与清晰的职责划分，提供了从连接建立、消息编解码、会话管理到消息分发与安全控制的完整链路。结合配置项可灵活切换发送器类型以适配不同部署环境。建议后续补充心跳、断线重连、连接池、监控与异常处理等能力，并在应用层完善消息加密与访问控制策略，以满足生产级需求。

## 附录
- 发送器API接口与实现：为业务模块提供统一的发送入口，支持对象序列化为JSON格式的消息内容。

章节来源
- [WebSocketSenderApi.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApi.java)
- [WebSocketSenderApiImpl.java](file://yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/api/websocket/WebSocketSenderApiImpl.java)