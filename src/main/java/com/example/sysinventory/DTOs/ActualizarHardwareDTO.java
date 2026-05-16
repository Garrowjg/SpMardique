package com.example.sysinventory.DTOs;

public class ActualizarHardwareDTO {

    private String ram;
    private String disco;
    private String procesador;
    private String mac;
    private String estadoFisico;
    private String descripcion; // descripción del cambio
    private String ingeniero;

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getDisco() { return disco; }
    public void setDisco(String disco) { this.disco = disco; }
    public String getProcesador() { return procesador; }
    public void setProcesador(String procesador) { this.procesador = procesador; }
    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }
    public String getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(String estadoFisico) { this.estadoFisico = estadoFisico; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIngeniero() { return ingeniero; }
    public void setIngeniero(String ingeniero) { this.ingeniero = ingeniero; }
}