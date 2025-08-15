## 一个伟大的AI助手项目
（AI Agent学习项目）

### 后端依赖项     

spring-boot-starter-parent@3.4.8       
Port: 8123        
Java21      
接口文档地址：http://127.0.0.1:8123/api/doc.html


| 名称                                                                                                         | 版本          | 描述                                 |
|------------------------------------------------------------------------------------------------------------|-------------|------------------------------------|
| spring-boot-starter-web                                                                                    | 3.4.8       | Springboot web库                    |
| lombok                                                                                                     | 5.8.37      | 简单方法注解化                            |
| [hutool-all](https://doc.hutool.cn/pages/index/)                                                           | 5.8.37      | 实用工具类                              |
| [knife4j](https://doc.xiaominfo.com/docs/quick-start)                                                      | 4.4.0       | 接口api文档生成                          |
| [dashscope-sdk-java](https://bailian.console.aliyun.com/)                                                  | 2.19.1      | 阿里云灵积大模型服务                         |
| [spring-ai-alibaba](https://java2ai.com/docs/1.0.0-M6.1/get-started/?spm=5176.29160081.0.0.2856aa5cPTxXQb) | 1.0.0-M6.1  | Spring AI 阿里版本                     |
| [langchain4j](https://docs.langchain4j.dev/intro/)                                                         | 1.0.0-beta2 | langchain4j，也是一种AI框架               |
| spring-ai-openai-spring-boot-starter                                                                       | 1.0.0-M6    | Spring AI OpenAI接入                 |
| jsonschema-generator                                                                                       | 4.37.0      | JSON格式生成                           |
| [kryo](https://github.com/EsotericSoftware/kryo)                                                           | 5.6.2       | 序列化库                               |
| spring-ai-markdown-document-reader                                                                         | 1.0.0-M6    | Markdown文件读取                       |
| spring-boot-starter-jdbc                                                                                   | -           | Spring JDBC依赖                      |
| postgresql                                                                                                 | runtime     | postgreSQL依赖                       |
| spring-ai-pgvector-store                                                                                   | 1.0.0-M6    | PGvector向量库扩展依赖                    |
| [jsoup](https://jsoup.org/)                                                                                | 1.19.1      | HTML解析库                            |
| [itext-core](https://itextpdf.com/)                                                                        | 9.1.0       | PDF生成核心库                           |
| [font-asian](https://itextpdf.com/)                                                                        | 9.1.0       | PDF亚洲字体支持                          |
| [okhttp](https://square.github.io/okhttp/)                                                                 | 4.12.0      | HTTP客户端库                           |
| spring-ai-mcp-client-spring-boot-starter                                                                   | 1.0.0-M6    | MCP客户端库                            |
| jakarta.mail                                                                                               | 2.0.1       | 邮件处理库                              |
| spring-ai-starter-mcp-server-webmvc                                                                        | 1.0.0-M7    | Spring AI 服务端MVC（M6版本有session管理问题） |


### 开发日志

2025年8月15日
1. 完成前端开发
   - 执行：``npm run dev``
   - 支持AI恋爱大师和“世另我”智能体应用，支持响应式布局
   - 优化后端发送Agent消息的行为，同时前端支持``思考``和``行动``两种样式的聊天气泡

2025年8月14日
1. 完成AI服务化接口开发
   - doChatWithLoveAppSync：同步方式调用Love App
   - doChatWithLoveAppSSE：SSE方式调用Love App
   - doChatWithManus：SSE方式调用超级智能体
2. 后端支持跨域

2025年8月13日
1. 基于[OpenManus](https://github.com/FoundationAgents/OpenManus)的思路实现了一个ReAct的智能体
   - BaseAgent：控制智能体的整体运行步骤，定义了智能体的状态，以及ReAct包含的think、act两个动作
   - ReActAgent：实现了step，即think和act两个操作的具体执行步骤
   - ToolCallAgent：实现了think的思考（调用什么工具），act的执行（执行工具，维护消息列表）
   - DdlwlrmaAgent：初始化参数和提示词，生成具体的外部调用接口

2025年8月12日
1. 实现了图片搜索MCP Server的开发
   - 服务端stdio部署就是打成jar包，在client直接配置jar包的运行方式
   - sse部署则相当于开一个远程的服务，可以使用云平台的serverless部署

2025年8月11日
1. 实现了MCP Client的开发
   - 需要在resources目录下配置MCP客户端

2025年8月9日
1. 打通LLM工具调用，使用统一注册器进行工具注册，工具正在开发中
   - [x] 文件处理工具
   - [x] 网页抓取工具
   - [x] 联网搜索工具
   - [x] 终端操作工具
   - [x] 资源下载工具
   - [x] PDF生成工具
   - [x] 邮箱信息获取工具([IMAP](https://mail.cszyy.cn/v2/help/detail?id=57)方式，需要优化安全逻辑)

2025年8月7日
1. 实现基于AI的文档元信息增强器
2. 实现基于AI的查询重写器
3. 实现自定义RAG检索增强顾问

2025年8月4日
1. 基于PostgreSQL.pgvector实现云端向量库的检索
   - 在服务器上安装并配置PostgreSQL，创建agentdb数据库
   - 为数据库安装pgvector、hstore插件
   - 手动配置VectorStore，Spring AI会自动在数据库中建表

2025年8月3日
1. 基于Spring AI实现RAG流程
   - 使用DocumentReader读取资源文件，加载一个Embedding模型，切分、向量化后存入VectorStore
   - 使用Q&A拦截器，向量化后对chunk打分，得分高的放进提示词里
   - 最终实现了基于两种方式（本地文件/百炼云端知识库服务）的RAG流程

2025年8月2日
1. 支持多轮对话记忆功能，并且可以区分不同的对话ID，自定义实现文件持久化ChatMemory
   - 将对话历史MessageList进行管理，在调用LLM时带进prompt
   - 持久化存储能够保证重启系统时，记忆不丢失
2. 自定义日志拦截器、re2拦截器实现
   - 在请求大模型前根据order顺序，链式调用拦截器的AdvisedRequest
   - 在请求后链式调用AdvisedResponse，对LLM输出进行处理
3. 实现结构化输出
   - 在prompt后拼接了目标结构的格式
   - 在接收到LLM的回复后，自动将文本解析成对应格式的对象
4. 使用PromptTemplate从文件中加载system prompt，可以参数化构造提示词
5. 支持dashscope-百炼/openai-modelscope两种调用方式

2025年8月1日
1. 项目初始化搭建
2. AI大模型调用的接入(/demo/invoke)：
   - SDK接入：官方提供的软件开发工具包，类似jar包的引入方式
   - HTTP接入：使用REST API发送HTTP请求调用
   - Spring AI：基于Spring生态的AI框架
   - LangChain4j：专注于构建LLM应用的Java框架