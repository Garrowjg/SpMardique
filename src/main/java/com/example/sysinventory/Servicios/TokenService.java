package com.example.sysinventory.Servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class TokenService {

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void clearToken() {
        this.accessToken = null;
    }

    public boolean hasToken() {
        return accessToken != null && !accessToken.isBlank();
    }
}