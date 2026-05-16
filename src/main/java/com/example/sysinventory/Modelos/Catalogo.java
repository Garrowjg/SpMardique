package com.example.sysinventory.Modelos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Colección 'catalogos' en MongoDB.
 * Cada documento representa un tipo de catálogo:
 *   tipo = "areas" | "cargos" | "opciones_ram" | "opciones_disco" | "marcas"
 *
 * Para marcas, cada valor es el nombre de la marca.
 * Los modelos por marca se guardan en documentos con tipo = "modelos_<marca>".
 */
@Document(collection = "catalogos")
public class Catalogo {

    @Id
    private String id;

    private String tipo;          // identificador del catálogo
    private List<String> valores = new ArrayList<>();

    public Catalogo() {}

    public Catalogo(String tipo, List<String> valores) {
        this.tipo    = tipo;
        this.valores = valores != null ? valores : new ArrayList<>();
    }

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public String getTipo()                { return tipo; }
    public void   setTipo(String tipo)     { this.tipo = tipo; }

    public List<String> getValores()                   { return valores; }
    public void         setValores(List<String> v)     { this.valores = v != null ? v : new ArrayList<>(); }
}