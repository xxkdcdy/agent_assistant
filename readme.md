## 一个伟大的AI助手项目
（AI Agent学习项目）

### 后端依赖项     

spring-boot-starter-parent@3.4.8       
Port: 8123        
Java21      
接口文档地址：http://127.0.0.1:8123/api/doc.html


| 名称                                                                                                         | 版本          | 描述                   |
|------------------------------------------------------------------------------------------------------------|-------------|----------------------|
| spring-boot-starter-web                                                                                    | 3.4.8       | Springboot web库      |
| lombok                                                                                                     | 5.8.37      | 简单方法注解化              |
| [hutool-all](https://doc.hutool.cn/pages/index/)                                                           | 5.8.37      | 实用工具类                |
| [knife4j](https://doc.xiaominfo.com/docs/quick-start)                                                      | 4.4.0       | 接口api文档生成            |
| [dashscope-sdk-java](https://bailian.console.aliyun.com/)                                                  | 2.19.1      | 阿里云灵积大模型服务           |
| [spring-ai-alibaba](https://java2ai.com/docs/1.0.0-M6.1/get-started/?spm=5176.29160081.0.0.2856aa5cPTxXQb) | 1.0.0-M6.1  | Spring AI 阿里版本       |
| [langchain4j](https://docs.langchain4j.dev/intro/)                                                         | 1.0.0-beta2 | langchain4j，也是一种AI框架 |
| spring-ai-openai-spring-boot-starter                                                                       | 1.0.0-M6    | Spring AI OpenAI接入   |
| jsonschema-generator                                                                                       | 4.37.0      | JSON格式生成             |
| [kryo](https://github.com/EsotericSoftware/kryo)                                                           | 5.6.2       | 序列化库                 |

### 开发日志

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