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

### 开发日志

2025年8月1日
1. 项目初始化搭建
2. AI大模型调用的接入(/demo/invoke)：
   - SDK接入：官方提供的软件开发工具包，类似jar包的引入方式
   - HTTP接入：使用REST API发送HTTP请求调用
   - Spring AI：基于Spring生态的AI框架
   - LangChain4j：专注于构建LLM应用的Java框架