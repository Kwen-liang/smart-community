package com.smartcommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 关键点：扫描 mapper 包，这样 MyBatis 才能找到你的接口
@MapperScan("com.smartcommunity.mapper")
public class SmartCommunityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCommunityBackendApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  智慧社区后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}