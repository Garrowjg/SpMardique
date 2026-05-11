package com.example.sysinventory.DTOs;

public class PerifericoDTO {
    private String tipo;
    private String marca;
    private String modelo;
    private String serial;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
}