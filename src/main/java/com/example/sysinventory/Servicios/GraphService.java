package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GraphService {

    @Value("${azure.sharepoint-embed-url}")
    private String sharepointEmbedUrl;

    /**
     * Construye la URL de embebido de SharePoint.
     * No añade el token porque SharePoint no lo acepta en la URL.
     * La autenticación se hace mediante cookies de sesión.
     */
    public String obtenerEmbedUrl(String serial, String accessToken) {
        if (sharepointEmbedUrl == null || sharepointEmbedUrl.isBlank()) {
            return "";
        }

        // La URL base ya debe incluir el sourcedoc y action=embedview
        // Solo añadimos parámetros adicionales útiles
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(sharepointEmbedUrl);

        // Parámetros opcionales para mejorar la experiencia
        builder.queryParam("wdHideHeaders", "True")
                .queryParam("wdDownloadButton", "True")
                .queryParam("wdInConfigurator", "True")
                .queryParam("wdAllowInteractivity", "True");

        // Si la URL base no contiene ActiveCell, lo añadimos (útil si el serial se usa para ir a la celda)
        if (!sharepointEmbedUrl.contains("ActiveCell")) {
            builder.queryParam("ActiveCell", serial + "!A1");
        }
        if (!sharepointEmbedUrl.contains("wdSheet")) {
            builder.queryParam("wdSheet", serial);
        }

        return builder.toUriString();
    }
}