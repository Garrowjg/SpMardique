package com.example.sysinventory.DTOs;

import com.example.sysinventory.Modelos.Equipo;
import java.util.List;

public class AreaDashboardDTO {

    private String nombre;
    private int total;
    private long activos;
    private long enMantenimiento;
    private long dadosDeBaja;
    private List<Equipo> equipos;

    public AreaDashboardDTO(String nombre, int total, long activos,
                            long enMantenimiento, long dadosDeBaja, List<Equipo> equipos) {
        this.nombre = nombre;
        this.total = total;
        this.activos = activos;
        this.enMantenimiento = enMantenimiento;
        this.dadosDeBaja = dadosDeBaja;
        this.equipos = equipos;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public long getActivos() { return activos; }
    public void setActivos(long activos) { this.activos = activos; }
    public long getEnMantenimiento() { return enMantenimiento; }
    public void setEnMantenimiento(long enMantenimiento) { this.enMantenimiento = enMantenimiento; }
    public long getDadosDeBaja() { return dadosDeBaja; }
    public void setDadosDeBaja(long dadosDeBaja) { this.dadosDeBaja = dadosDeBaja; }
    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }
}