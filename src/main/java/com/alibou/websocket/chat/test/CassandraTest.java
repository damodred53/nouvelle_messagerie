package com.alibou.websocket.chat.test;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CassandraTest implements CommandLineRunner {

    private final CqlSession cqlSession;

    public CassandraTest(CqlSession cqlSession) {
        this.cqlSession = cqlSession;
    }

    @Override
    public void run(String... args) {
        cqlSession.execute(
                "INSERT INTO my_logs.logs (id, message, level, logger, timestamp) VALUES (uuid(), 'Coucou', 'INFO', 'CassandraTest', toTimestamp(now()))");
        System.out.println("Log inséré dans Cassandra !");
    }
}
