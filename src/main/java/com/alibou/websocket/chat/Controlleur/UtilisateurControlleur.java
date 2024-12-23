package com.alibou.websocket.chat.Controlleur;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public List<UtilisateurResponse> findAll() {
        log.debug("Finding all users ...");

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
