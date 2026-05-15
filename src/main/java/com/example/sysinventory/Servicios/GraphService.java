package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GraphService {

    @Value("${azure.sharepoint-embed-url}")
    private String sharepointEmbedUrl;

    /**
     * Construye la URL de embed de SharePoint.
     *
     * Si se provee un accessToken válido (obtenido del OAuth2 de Spring),
     * lo inyecta en la URL para que SharePoint no necesite abrir ningún
     * popup de autenticación dentro del iframe — esto resuelve el bug en móvil.
     *
     * Si el token es null o vacío, devuelve la URL sin token (comportamiento anterior).
     */
    public String obtenerEmbedUrl(String serial, String accessToken) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(sharepointEmbedUrl)
                .queryParam("wdHideHeaders", "True")
                .queryParam("wdDownloadButton", "True")
                .queryParam("wdInConfigurator", "True")
                .queryParam("wdAllowInteractivity", "True")
                .queryParam("ActiveCell", serial + "!A1")
                .queryParam("wdSheet", serial);

        // Inyectar el access token para evitar el popup de login en móvil
        if (accessToken != null && !accessToken.isBlank()) {
            builder.queryParam("access_token", accessToken);
        }

        return builder.toUriString();
    }
}