package com.example.sysinventory.DTOs;

import jakarta.validation.constraints.NotBlank;

public class CambioPropietarioDTO {

    @NotBlank(message = "El nuevo responsable es obligatorio")
    private String nuevoResponsable;

    @NotBlank(message = "El nuevo cargo es obligatorio")
    private String nuevoCargo;

    private String nuevaArea;
    private String motivo;
    private String ingeniero;

    public String getNuevoResponsable() { return nuevoResponsable; }
    public void setNuevoResponsable(String nuevoResponsable) { this.nuevoResponsable = nuevoResponsable; }
    public String getNuevoCargo() { return nuevoCargo; }
    public void setNuevoCargo(String nuevoCargo) { this.nuevoCargo = nuevoCargo; }
    public String getNuevaArea() { return nuevaArea; }
    public void setNuevaArea(String nuevaArea) { this.nuevaArea = nuevaArea; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getIngeniero() { return ingeniero; }
    public void setIngeniero(String ingeniero) { this.ingeniero = ingeniero; }
}

