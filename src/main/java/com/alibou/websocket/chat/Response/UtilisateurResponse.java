package com.alibou.websocket.chat.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
//définis les propriétés d'un UtilisateurResponse
public class UtilisateurResponse {
    private String id;
    private String username;
}
