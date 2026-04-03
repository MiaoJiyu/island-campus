package com.islandcampus.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.islandcampus.server")
@EnableScheduling
public class IslandCampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(IslandCampusApplication.class, args);
        System.out.println("========================================");
        System.out.println("  灵岛校园 IslandCampus v2.1 启动成功");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
