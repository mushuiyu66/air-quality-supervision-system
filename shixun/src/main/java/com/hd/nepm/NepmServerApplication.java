package com.hd.nepm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName:
 * Package: com.hd.nepm
 * Description：
 *
 * @Author msy
 * @Create 2026/6/23 14:22
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.hd.nepm.mapper")
public class NepmServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NepmServerApplication.class, args);
    }
}
