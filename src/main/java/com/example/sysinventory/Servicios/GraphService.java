package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}