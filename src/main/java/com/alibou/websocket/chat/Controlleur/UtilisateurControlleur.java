package com.alibou.websocket.chat.Controlleur;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibou.websocket.chat.LogService.LogService;
import com.alibou.websocket.chat.Models.Utilisateur;
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
    private LogService logService;

    @GetMapping
    public List<UtilisateurResponse> findAll() {
        log.debug("Finding all users ...");
        // Insère un log personnalisé avec des informations sur cette opération.
        logService.insertLog("L'ensemble des utilisateurs ont été récupérés", "INFO", "ChatController", "Controller");
        // Récupère tous les utilisateurs, les convertit en `UtilisateurResponse`
        return this.utilisateurRepository.findAll()
                .stream()
                .map(this::convert)
                .toList();
    }

    private UtilisateurResponse convert(Utilisateur utilisateur) {
        UtilisateurResponse resp = UtilisateurResponse.builder().build();

        // Copie les propriétés de l'objet `Utilisateur` dans l'objet UtilisateurResponse.

        BeanUtils.copyProperties(utilisateur, resp);

        return resp;
    }
}
