package com.example.homestaybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.homestaybackend.mapper")
@EnableScheduling // 启用定时任务
public class HomestayBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomestayBackendApplication.class, args);
    }

}
