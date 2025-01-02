package com.alibou.websocket.chat.RepositoryCassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.alibou.websocket.chat.ModelsCassandra.Log;

@Repository("logCassandraRepository")
public interface LogRepository extends CassandraRepository<Log, Long> {

}
