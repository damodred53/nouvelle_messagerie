package com.alibou.websocket.chat.service;

import com.alibou.websocket.chat.Models.Log;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LogService {

    private final CqlSession cqlSession;

    public LogService(CqlSession cqlSession) {
        this.cqlSession = cqlSession;
    }

    // Méthode pour insérer un log dans Cassandra via CqlSession
    public void insertLog(String message, String level, String logger, String sender) {
        // Créer un UUID pour l'ID du log
        UUID id = UUID.randomUUID();

        // Créer une requête CQL pour insérer le log dans la table "logs"
        String cql = "INSERT INTO my_logs.logs (id, message, level, logger, sender, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, toTimestamp(now()))";

        // Exécuter la requête en passant les paramètres
        cqlSession.execute(cql, id, message, level, logger, sender);
        System.out.println("Log inséré dans Cassandra avec CqlSession !");
    }
}
