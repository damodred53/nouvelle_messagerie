package com.alibou.websocket.chat.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alibou.websocket.chat.Models.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    public Optional<Utilisateur> findByUsername(String username);

}
