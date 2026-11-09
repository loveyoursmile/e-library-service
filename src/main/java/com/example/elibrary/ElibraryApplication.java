package com.example.elibrary;

import com.example.elibrary.domain.LoanPolicy;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 启动类。这里只注册两个无状态的基础设施 Bean：
 * Clock 让借期与逾期可测试，LoanPolicy 是领域服务（本身不依赖 Spring）。
 */
@SpringBootApplication
public class ElibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElibraryApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public LoanPolicy loanPolicy() {
        return new LoanPolicy();
    }
}
