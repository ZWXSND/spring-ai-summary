package com.glmapper.ai.tc.tools.methods;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.tool.annotation.Tool;

import java.io.FileInputStream;

/**
 * @Classname FileReaderTools
 * @Description 读取文件工具（支持文本文件和 Word 文档）
 * @Date 2025/5/29 15:06
 * @Created by glmapper
 */
public class FileReaderTools {

    @Tool(description = "Read a file and print its content. Supports text files (.txt, .md, .java, .py, etc.) and Word documents (.docx)")
    public String readFileAndPrint(String filePath) {
        System.out.println("🔧 [FileReaderTools] 开始读取文件: " + filePath);
        
        // 先尝试作为绝对路径或相对路径读取
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            
            // 检查文件是否存在
            if (!java.nio.file.Files.exists(path)) {
                System.out.println("❌ [FileReaderTools] 文件不存在: " + path.toAbsolutePath());
                
                // 如果不存在，尝试从 classpath 读取
                return readFromClasspath(filePath);
            }
            
            // 检查是否可读
            if (!java.nio.file.Files.isReadable(path)) {
                String error = "文件不可读: " + path.toAbsolutePath();
                System.out.println("❌ [FileReaderTools] " + error);
                return error;
            }
            
            // 根据文件扩展名决定如何读取
            String fileName = path.getFileName().toString().toLowerCase();
            String content;
            
            if (fileName.endsWith(".docx")) {
                // 读取 Word 文档
                content = readWordDocument(path.toString());
            } else {
                // 读取文本文件
                content = java.nio.file.Files.readString(path);
            }
            
            return content;
            
        } catch (Exception e) {
            System.out.println("⚠️ [FileReaderTools] 从文件系统读取失败: " + e.getMessage());
            e.printStackTrace();
            
            // 尝试从 classpath 读取
            return readFromClasspath(filePath);
        }
    }
    
    /**
     * 读取 Word 文档
     */
    private String readWordDocument(String filePath) {
        System.out.println("📄 [FileReaderTools] 开始读取 Word 文档");
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String text = extractor.getText();
            return text;
            
        } catch (Exception e) {
            String error = "读取 Word 文档失败: " + e.getMessage();
            System.out.println("❌ [FileReaderTools] " + error);
            e.printStackTrace();
            return error;
        }
    }
    
    /**
     * 从 classpath 读取文件
     */
    private String readFromClasspath(String filePath) {
        System.out.println("🔄 [FileReaderTools] 尝试从 classpath 读取: " + filePath);
        
        try {
            java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            
            if (inputStream == null) {
                String error = "文件在文件系统和 classpath 中都找不到: " + filePath;
                System.out.println("❌ [FileReaderTools] " + error);
                return error;
            }
            
            String content = new String(inputStream.readAllBytes());
            System.out.println("✅ [FileReaderTools] 从 classpath 成功读取，大小: " + content.length() + " 字符");
            
            return content;
            
        } catch (Exception ex) {
            String error = "从 classpath 读取文件失败: " + ex.getMessage();
            System.out.println("❌ [FileReaderTools] " + error);
            ex.printStackTrace();
            return error;
        }
    }
}
