package com.alibou.websocket.chat.Controlleur;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibou.websocket.chat.Models.Log;
import com.alibou.websocket.chat.Models.Utilisateur;
import com.alibou.websocket.chat.Repository.LogRepository;
import com.alibou.websocket.chat.Repository.UtilisateurRepository;
import com.alibou.websocket.chat.Response.UtilisateurResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
public class UtilisateurControlleur {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LogRepository logRepository;

    @GetMapping
    public List<UtilisateurResponse> findAll() {
        log.debug("Finding all users ...");

        // Créer un log avec le message et l'heure actuelle
        Log log = Log.builder()
                .level("INFO") // Niveau de log
                .logger("ChatController") // Indiquer la source du log
                .message("L'ensemble des utilisateurs ont été récupérés") // Message spécifique
                .timestamp(LocalDateTime.now()) // Utiliser l'heure actuelle
                .build();

        // Enregistrer le log dans la base de données PostgreSQL
        logRepository.save(log);

        return this.utilisateurRepository.findAll()
                .stream()
                .map(this::convert)
                .toList();
    }

    private UtilisateurResponse convert(Utilisateur utilisateur) {
        UtilisateurResponse resp = UtilisateurResponse.builder().build();

        BeanUtils.copyProperties(utilisateur, resp);

        return resp;
    }
}
