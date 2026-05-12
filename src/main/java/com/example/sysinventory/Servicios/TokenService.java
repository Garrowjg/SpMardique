package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class TokenService {

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.tenant-id}")
    private String tenantId;

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