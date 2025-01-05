package com.alibou.websocket.chat.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfig {

    @Bean
    public CqlSession cqlSession() {
        return CqlSession.builder().build();
    }
}
