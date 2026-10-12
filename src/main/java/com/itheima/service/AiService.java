package com.itheima.service;

import java.util.function.Consumer;
public interface AiService {
    // 调用AI大模型生成回答
    String chat(String message);

    // 新增流式回答：每收到一段回答，就通过consumer传出去
    void streamChat(String message,Consumer<String> onChunk);
}
