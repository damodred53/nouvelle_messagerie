package com.alibou.websocket.chat.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alibou.websocket.chat.Models.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    public boolean existsByUsername(String username);
}
