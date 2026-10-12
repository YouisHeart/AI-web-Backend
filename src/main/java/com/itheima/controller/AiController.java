package com.itheima.controller;

import com.itheima.pojo.ChatRequest;
import com.itheima.pojo.Result;
import com.itheima.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 普通问答接口：保留原有功能
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {

        if (request == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {
            return Result.error("问题不能为空");
        }

        try {
            String answer = aiService.chat(request.getMessage().trim());
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("AI 服务调用失败，请稍后重试");
        }
    }

    /**
     * 流式问答接口
     */
    @PostMapping(
            value = "/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamChat(@RequestBody ChatRequest request) {

        if (request == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        // 0 表示不设置超时时间；生产环境建议配置合理的超时和取消机制
        SseEmitter emitter = new SseEmitter(0L);

        // 在线程池中执行，避免占用 Servlet 请求线程
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                aiService.streamChat(
                        request.getMessage().trim(),
                        chunk -> {
                            try {
                                emitter.send(
                                        SseEmitter.event().data(chunk)
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(
                                        "向前端发送数据失败", e
                                );
                            }
                        }
                );

                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(
                            SseEmitter.event()
                                    .name("app-error")
                                    .data("AI 服务调用失败，请稍后重试")
                    );
                } catch (Exception ignored) {
                    // 前端可能已经断开连接
                }

                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}