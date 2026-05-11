package com.example.sysinventory.DTOs;

import jakarta.validation.constraints.NotBlank;

public class HistorialEventoDTO {

    @NotBlank(message = "El evento es obligatorio")
    private String evento;

    @NotBlank(message = "El ingeniero es obligatorio")
    private String ingeniero;

    private String tipo;

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getIngeniero() { return ingeniero; }
    public void setIngeniero(String ingeniero) { this.ingeniero = ingeniero; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

