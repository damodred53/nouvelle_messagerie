package com.alibou.websocket.chat.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UtilisateurResponse {
    private String id;
    private String username;
}
