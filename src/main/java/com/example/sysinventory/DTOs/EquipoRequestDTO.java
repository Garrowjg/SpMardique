package com.example.sysinventory.DTOs;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class EquipoRequestDTO {

    @NotBlank(message = "El área es obligatoria")
    private String area;

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;

    @NotBlank(message = "El responsable es obligatorio")
    private String responsable;

    private String tipoDispositivo;
    private String marca;
    private String modelo;
    private String serial;
    private String mac;
    private String ram;
    private String disco;
    private String procesador;
    private String estadoFisico;
    private String estado;
    private String observaciones;

    private boolean esPrestamo;
    private boolean incluyeCargador;
    private LocalDate fechaEntrega;

    private String cargadorMarca;
    private String cargadorModelo;
    private String cargadorSerial;

    private List<PerifericoDTO> perifericos;

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getTipoDispositivo() { return tipoDispositivo; }
    public void setTipoDispositivo(String tipoDispositivo) { this.tipoDispositivo = tipoDispositivo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getDisco() { return disco; }
    public void setDisco(String disco) { this.disco = disco; }
    public String getProcesador() { return procesador; }
    public void setProcesador(String procesador) { this.procesador = procesador; }
    public String getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(String estadoFisico) { this.estadoFisico = estadoFisico; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isEsPrestamo() { return esPrestamo; }
    public void setEsPrestamo(boolean esPrestamo) { this.esPrestamo = esPrestamo; }
    public boolean isIncluyeCargador() { return incluyeCargador; }
    public void setIncluyeCargador(boolean incluyeCargador) { this.incluyeCargador = incluyeCargador; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getCargadorMarca() { return cargadorMarca; }
    public void setCargadorMarca(String cargadorMarca) { this.cargadorMarca = cargadorMarca; }
    public String getCargadorModelo() { return cargadorModelo; }
    public void setCargadorModelo(String cargadorModelo) { this.cargadorModelo = cargadorModelo; }
    public String getCargadorSerial() { return cargadorSerial; }
    public void setCargadorSerial(String cargadorSerial) { this.cargadorSerial = cargadorSerial; }
    public List<PerifericoDTO> getPerifericos() { return perifericos; }
    public void setPerifericos(List<PerifericoDTO> perifericos) { this.perifericos = perifericos; }
}