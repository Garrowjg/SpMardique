package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GraphService {

    @Value("${azure.sharepoint-embed-url}")
    private String sharepointEmbedUrl;


    public String obtenerEmbedUrl(String serial, String token) {

        return sharepointEmbedUrl
                + "&wdHideHeaders=True"
                + "&wdDownloadButton=True"
                + "&wdInConfigurator=True"
                + "&wdAllowInteractivity=True"
                + "&ActiveCell=" + serial + "!A1"
                + "&wdSheet=" + serial;
    }

    @Value("${azure.inventario-embed-url}")
    private String inventarioEmbedUrl;

    public String obtenerInventarioEmbedUrl() {
        if (inventarioEmbedUrl == null || inventarioEmbedUrl.isBlank()) {
            return "";
        }

        // Añadir parámetros opcionales para mejorar la experiencia en el iframe
        return UriComponentsBuilder.fromUriString(inventarioEmbedUrl)
                .queryParam("wdHideHeaders", "True")
                .queryParam("wdDownloadButton", "True")
                .queryParam("wdInConfigurator", "True")
                .queryParam("wdAllowInteractivity", "True")
                .queryParam("wdFitToPage", "True")
                .build()
                .toUriString();
    }

    public String obtenerEmbedUrlFormato(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return org.springframework.web.util.UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("wdHideHeaders", "True")
                .queryParam("wdDownloadButton", "True")
                .queryParam("wdInConfigurator", "True")
                .queryParam("wdAllowInteractivity", "True")
                .queryParam("wdFitToPage", "True")
                .build()
                .toUriString();
    }
}