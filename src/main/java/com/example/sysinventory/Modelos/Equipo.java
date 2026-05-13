package com.example.sysinventory.Modelos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "equipos")
public class Equipo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String area;
    private String cargo;
    private String responsable;
    private LocalDate fechaEntrega;
    private boolean esPrestamo;

    private String tipoDispositivo;   // Portátil, Desktop, Todo en uno
    private String marca;
    private String modelo;
    private String serial;
    private String mac;               // Dirección MAC
    private String ram;
    private String disco;
    private String estadoFisico;      // Nuevo, Usado, Funcional, Dañado
    private String observaciones;

    private String estado;            // Activo, En mantenimiento, Dado de baja

    private boolean incluyeCargador;
    private String cargadorMarca;
    private String cargadorModelo;
    private String cargadorSerial;

    private List<Periferico> perifericos = new ArrayList<>();

    private List<HistorialEvento> historial = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public boolean isEsPrestamo() { return esPrestamo; }
    public void setEsPrestamo(boolean esPrestamo) { this.esPrestamo = esPrestamo; }

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

    public String getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(String estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isIncluyeCargador() { return incluyeCargador; }
    public void setIncluyeCargador(boolean incluyeCargador) { this.incluyeCargador = incluyeCargador; }

    public String getCargadorMarca() { return cargadorMarca; }
    public void setCargadorMarca(String cargadorMarca) { this.cargadorMarca = cargadorMarca; }

    public String getCargadorModelo() { return cargadorModelo; }
    public void setCargadorModelo(String cargadorModelo) { this.cargadorModelo = cargadorModelo; }

    public String getCargadorSerial() { return cargadorSerial; }
    public void setCargadorSerial(String cargadorSerial) { this.cargadorSerial = cargadorSerial; }

    public List<Periferico> getPerifericos() { return perifericos; }
    public void setPerifericos(List<Periferico> perifericos) { this.perifericos = perifericos != null ? perifericos : new ArrayList<>(); }

    public List<HistorialEvento> getHistorial() { return historial; }
    public void setHistorial(List<HistorialEvento> historial) { this.historial = historial != null ? historial : new ArrayList<>(); }
}