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
     * No inyecta token; confía en la cookie de sesión.
     * Si el usuario no ha iniciado sesión en el navegador, SharePoint pedirá login en un popup.
     * En móvil, es mejor redirigir a la URL directamente (no iframe).
     */
    public String obtenerEmbedUrl(String serial, String accessToken) {
        // No usamos el token para evitar problemas de popups en móvil.
        return UriComponentsBuilder
                .fromUriString(sharepointEmbedUrl)
                .queryParam("wdHideHeaders", "True")
                .queryParam("wdDownloadButton", "True")
                .queryParam("wdInConfigurator", "True")
                .queryParam("wdAllowInteractivity", "True")
                .queryParam("ActiveCell", serial + "!A1")
                .queryParam("wdSheet", serial)
                .toUriString();
    }
}