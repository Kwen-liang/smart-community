package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用文件上传控制器
 */
@RestController
@RequestMapping("/common")
public class FileController {

    // 读取配置文件中的上传路径，如果没有配置则使用默认路径
    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    // 读取服务器访问地址前缀
    @Value("${server.base-url:http://localhost:8080/api/files/}")
    private String baseUrl;

    /**
     * 文件上传接口
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 1. 获取原文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 生成新文件名 (防止重名)
            String newFileName = UUID.randomUUID().toString() + suffix;

            // 3. 确保存储目录存在
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // 4. 保存文件
            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            // 5. 返回访问 URL
            // 注意：实际生产中需要配置静态资源映射，这里仅返回 URL 字符串
            String fileUrl = baseUrl + newFileName;
            return Result.success("上传成功", fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}