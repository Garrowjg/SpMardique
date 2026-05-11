package com.example.sysinventory.Modelos;

import java.time.LocalDate;

public class HistorialEvento {

    private LocalDate fecha;
    private String evento;
    private String ingeniero;
    private String tipo; // CAMBIO_PROPIETARIO, MANTENIMIENTO, HARDWARE, ESTADO, GENERAL

    public HistorialEvento() {}

    public HistorialEvento(LocalDate fecha, String evento, String ingeniero, String tipo) {
        this.fecha = fecha;
        this.evento = evento;
        this.ingeniero = ingeniero;
        this.tipo = tipo;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getIngeniero() { return ingeniero; }
    public void setIngeniero(String ingeniero) { this.ingeniero = ingeniero; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}