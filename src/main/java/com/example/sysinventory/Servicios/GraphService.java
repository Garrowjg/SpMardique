package com.example.sysinventory.Servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class GraphService {

    @Value("${azure.sharepoint-site}")
    private String sharepointSite;

    @Value("${azure.file-id}")
    private String fileId;

    public Map<String, String> leerHojaEquipo(String nombreHoja, String accessToken) {
        Map<String, String> datos = new LinkedHashMap<>();
        if (accessToken == null || accessToken.isBlank()) {
            datos.put("error", "No hay token de acceso. Inicia sesión en /auth/login");
            return datos;
        }
        try {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = "https://graph.microsoft.com/v1.0/sites/" + sharepointSite
                    + "/drive/items/" + fileId
                    + "/workbook/worksheets('" + nombreHoja + "')/usedRange";

            ResponseEntity<Map> response = rest.exchange(
                    URI.create(url), HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<List<Object>> values = (List<List<Object>>) response.getBody().get("values");
                if (values != null && !values.isEmpty()) {
                    datos = mapearFilasExcel(values);
                } else {
                    datos.put("error", "La hoja '" + nombreHoja + "' está vacía.");
                }
            } else {
                datos.put("error", "No se encontraron datos en la hoja '" + nombreHoja + "'");
            }
        } catch (Exception e) {
            datos.put("error", "Error al leer la hoja: " + e.getMessage());
        }
        return datos;
    }

    private Map<String, String> mapearFilasExcel(List<List<Object>> rows) {
        Map<String, String> datos = new LinkedHashMap<>();
        for (List<Object> row : rows) {
            if (row == null || row.isEmpty()) continue;
            String col0 = row.size() > 0 && row.get(0) != null ? row.get(0).toString().trim() : "";
            String col0Upper = col0.toUpperCase();

            if (col0Upper.contains("AREA DE ASIGNACION")) {
                datos.put("Area", getColValue(row, 1));
            } else if (col0Upper.contains("FECHA DE ASIGNACION")) {
                datos.put("Fecha asignacion", getColValue(row, 3));
            } else if (col0Upper.contains("FUNCIONARIO RESPONSABLE")) {
                datos.put("Responsable", getColValue(row, 1));
                datos.put("Cargo", getColValue(row, 3));
            } else if (col0Upper.equals("ESTADO")) {
                datos.put("Estado fisico", getColValue(row, 1));
            } else if (col0Upper.contains("NUMERO DE INVENTARIO")) {
                datos.put("Numero inventario", getColValue(row, 1));
            } else if (col0Upper.contains("MARCA") && col0Upper.contains("MODELO")) {
                datos.put("Marca Modelo", getColValue(row, 1));
            } else if (col0Upper.equals("SERIAL")) {
                datos.put("Serial", getColValue(row, 1));
            } else if (col0Upper.equals("PROCESADOR")) {
                datos.put("Procesador", getColValue(row, 1));
            } else if (col0Upper.contains("MEMORIA RAM")) {
                datos.put("RAM", getColValue(row, 1));
            } else if (col0Upper.contains("DISCO DURO")) {
                datos.put("Disco", getColValue(row, 1));
            } else if (col0Upper.contains("SISTEMA OPERATIVO")) {
                datos.put("Sistema operativo", getColValue(row, 1));
            } else if (col0Upper.contains("DIRECCION MAC")) {
                datos.put("MAC", getColValue(row, 1));
            } else if (col0Upper.equals("ANTIVIRUS")) {
                datos.put("Antivirus", getColValue(row, 1));
            } else if (col0Upper.equals("OFFICE")) {
                datos.put("Office", getColValue(row, 1));
            }
        }
        return datos;
    }

    private String getColValue(List<Object> row, int index) {
        if (row.size() > index && row.get(index) != null) {
            return row.get(index).toString().trim();
        }
        return "—";
    }
}