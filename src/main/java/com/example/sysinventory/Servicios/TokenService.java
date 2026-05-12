package com.example.sysinventory.Servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class TokenService {

    private final String clientId = "8a2c149d-c019-4287-b370-f545aaa60f2a";
    private final String clientSecret ="piS8Q~C8Xcdin3mmj3kH~DdL5FFQcaPElwsqAawp";
    private final String tenantId = "f33bed9f-22bd-4350-b7da-66bd88fc6458";

    private String accessToken;
    private String refreshToken;
    private long expiresAt = 0;

    public synchronized String getAccessToken() {
        if (System.currentTimeMillis() > expiresAt && refreshToken != null) {
            refreshAccessToken();
        }
        return accessToken;
    }

    public void fetchTokens(String code, String redirectUri) {
        RestTemplate rest = new RestTemplate();
        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&redirect_uri=" + redirectUri
                + "&grant_type=authorization_code";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = rest.postForEntity(url, request, Map.class);
        if (response.getBody() != null) {
            this.accessToken = (String) response.getBody().get("access_token");
            this.refreshToken = (String) response.getBody().get("refresh_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            if (expiresIn != null) {
                this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
            }
        }
    }

    private void refreshAccessToken() {
        if (refreshToken == null) return;
        RestTemplate rest = new RestTemplate();
        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken
                + "&grant_type=refresh_token";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = rest.postForEntity(url, request, Map.class);
        if (response.getBody() != null) {
            this.accessToken = (String) response.getBody().get("access_token");
            this.refreshToken = (String) response.getBody().get("refresh_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            if (expiresIn != null) {
                this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
            }
        }
    }
}