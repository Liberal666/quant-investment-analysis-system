package com.example.quant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.quant.mapper")
@SpringBootApplication
public class QuantApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuantApplication.class, args);
    }
}
