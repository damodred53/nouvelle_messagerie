package com.alibou.websocket.chat.LogService;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class LogService {

    private final CqlSession cqlSession;

    public LogService(CqlSession cqlSession) {
        this.cqlSession = cqlSession;
    }

    // Méthode pour insérer un log dans Cassandra via CqlSession
    public void insertLog(String message, String level, String logger, String sender) {

        UUID id = UUID.randomUUID();

        String cql = "INSERT INTO my_logs.logs (id, message, level, logger, sender, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, toTimestamp(now()))";

        cqlSession.execute(cql, id, message, level, logger, sender);
        System.out.println("Log inséré dans Cassandra avec CqlSession !");
    }
}
