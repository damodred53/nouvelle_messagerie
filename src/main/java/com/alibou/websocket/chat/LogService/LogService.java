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

    // Méthode pour insérer un log à l'intérieur de Cassandra via CqlSession
    public void insertLog(String message, String level, String logger, String sender) {
        // Génère un identifiant unique pour chaque log
        UUID id = UUID.randomUUID();
    // Requête CQL pour insérer un log dans la table Cassandra.
        String cql = "INSERT INTO my_logs.logs (id, message, level, logger, sender, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, toTimestamp(now()))";

        cqlSession.execute(cql, id, message, level, logger, sender);
        // Affiche un message de confirmation dans la console.
        System.out.println("Log inséré dans Cassandra avec CqlSession !");
    }
}
