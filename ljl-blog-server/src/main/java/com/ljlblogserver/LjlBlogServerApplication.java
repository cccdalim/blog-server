package com.ljlblogserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ljlblogserver.mapper")
public class LjlBlogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LjlBlogServerApplication.class, args);
    }

}
