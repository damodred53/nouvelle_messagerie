package com.alibou.websocket.chat.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alibou.websocket.chat.Models.Log;
// Simplifie les requêtes
public interface LogRepository extends JpaRepository<Log, Long> {

}
