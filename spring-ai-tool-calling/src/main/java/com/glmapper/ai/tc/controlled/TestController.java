package com.glmapper.ai.tc.controlled;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/testTool")
public class TestController {

    @Resource
    private UserControlledExecutor userControlledExecutor;

    @Autowired
    private ChatClient chatClient;



    /**
     * 流式输出：上传文件并回答问题（使用 Flux）
     *
     * @param file 上传的文件
     * @param question 问题
     * @return 流式返回 AI 回答
     */
    @PostMapping(value = "/chat/upload/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> uploadFileAndAnswerStream(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("question") String question) {
        try {
            // 读取文件内容
            String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename();
            
            // 构建提示词
            String prompt = String.format(
                "以下是文件 '%s' 的内容：\n\n%s\n\n请基于上述文件内容回答问题：%s",
                fileName, fileContent, question
            );
            
            // 返回流式响应
            return chatClient.prompt(prompt).stream().content();
        } catch (Exception e) {
            return Flux.just("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 流式输出：使用工具调用方式（返回 Flux）
     * 先保存文件，让 AI 调用工具读取，然后流式返回答案
     *
     * @param file 上传的文件
     * @param question 问题
     * @return 流式返回 AI 回答
     */
    @PostMapping(value = "/chat/upload/stream-with-tool", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> uploadWithToolCallingStream(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("question") String question) {
        try {
            // 保存文件到临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = file.getOriginalFilename();
            // 修复路径拼接问题
            if (!tempDir.endsWith("/") && !tempDir.endsWith("\\")) {
                tempDir = tempDir + "/";
            }
            String filePath = tempDir + fileName;
            java.io.File dest = new java.io.File(filePath);
            file.transferTo(dest);
            
            // 构建提示词
            String prompt = String.format(
                "文件路径是：%s\n请读取这个文件的内容，然后回答问题：%s",
                filePath, question
            );
            
            // 返回流式响应，并在完成时清理文件
            return chatClient.prompt(prompt)
                    .stream()
                    .content()
                    .doFinally(signalType -> dest.delete());
            
        } catch (Exception e) {
            return Flux.just("处理失败: " + e.getMessage());
        }
    }
}
