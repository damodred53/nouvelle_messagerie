package com.alibou.websocket.chat.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibou.websocket.chat.LogService.LogService;

@Component
public class CassandraTest implements CommandLineRunner {

    private final LogService logService;

    public CassandraTest(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void run(String... args) {
        // Utiliser le service pour insérer un log dans Cassandra via CqlSession
        logService.insertLog("Coucou ça marche !!", "INFO", "CassandraTest", "Florent");
    }
}
