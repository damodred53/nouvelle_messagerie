package com.alibou.websocket.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.alibou.websocket.chat.Repository") // Package pour les repositories
                                                                                    // Cassandra
@EnableJpaRepositories(basePackages = "com.alibou.websocket.chat.Repository") // Package pour les repositories JPA
public class RepositoryConfig {
    // Cette classe configure Spring pour gérer à la fois les repositories JPA et
    // Cassandra
}
