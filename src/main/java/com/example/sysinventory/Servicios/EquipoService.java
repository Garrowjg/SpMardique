package com.example.sysinventory.Servicios;

import com.example.sysinventory.DTOs.*;
import com.example.sysinventory.Modelos.Equipo;
import com.example.sysinventory.Modelos.HistorialEvento;
import com.example.sysinventory.Modelos.Periferico;
import com.example.sysinventory.Repositorio.EquipoRepository;
import com.example.sysinventory.Utilidades.CodigoGenerador;
import com.example.sysinventory.Utilidades.QRGenerador;
import com.google.zxing.WriterException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final CodigoGenerador codigoGenerador;
    private final QRGenerador qrGenerador;

    public EquipoService(EquipoRepository equipoRepository,
                         CodigoGenerador codigoGenerador,
                         QRGenerador qrGenerador) {
        this.equipoRepository = equipoRepository;
        this.codigoGenerador  = codigoGenerador;
        this.qrGenerador      = qrGenerador;
    }

    // ── Listar / buscar ───────────────────────────────────────────────────
    public List<Equipo> listarTodos() {
        List<Equipo> lista = equipoRepository.findAll();
        return lista != null ? lista : new ArrayList<>();
    }

    public Equipo buscarPorCodigo(String codigo) {
        return equipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + codigo));
    }

    public List<Equipo> buscarPorArea(String area) {
        return equipoRepository.findByArea(area);
    }

    public List<Equipo> buscarPorEstado(String estado) {
        return equipoRepository.findByEstado(estado);
    }

    // ── Crear ─────────────────────────────────────────────────────────────
    public Equipo crear(EquipoRequestDTO dto) {
        String codigo = codigoGenerador.generar(dto.getArea(), dto.getCargo());

        Equipo equipo = new Equipo();
        equipo.setCodigo(codigo);
        equipo.setArea(dto.getArea());
        equipo.setCargo(dto.getCargo());
        equipo.setResponsable(dto.getResponsable());
        equipo.setFechaEntrega(dto.getFechaEntrega());
        equipo.setEsPrestamo(dto.isEsPrestamo());
        equipo.setTipoDispositivo(dto.getTipoDispositivo());
        equipo.setMarca(dto.getMarca());
        equipo.setModelo(dto.getModelo());
        equipo.setSerial(dto.getSerial());
        equipo.setMac(dto.getMac());
        equipo.setRam(dto.getRam());
        equipo.setDisco(dto.getDisco());
        equipo.setEstadoFisico(dto.getEstadoFisico());
        equipo.setEstado(dto.getEstado() != null && !dto.getEstado().isBlank() ? dto.getEstado() : "Activo");
        equipo.setObservaciones(dto.getObservaciones());
        equipo.setIncluyeCargador(dto.isIncluyeCargador());
        equipo.setCargadorMarca(dto.getCargadorMarca());
        equipo.setCargadorModelo(dto.getCargadorModelo());
        equipo.setCargadorSerial(dto.getCargadorSerial());

        // Periféricos
        List<Periferico> perifericos = new ArrayList<>();
        if (dto.getPerifericos() != null) {
            for (PerifericoDTO p : dto.getPerifericos()) {
                if (p.getTipo() != null && !p.getTipo().isBlank()) {
                    Periferico peri = new Periferico();
                    peri.setTipo(p.getTipo());
                    peri.setMarca(p.getMarca());
                    peri.setModelo(p.getModelo());
                    peri.setSerial(p.getSerial());
                    perifericos.add(peri);
                }
            }
        }
        equipo.setPerifericos(perifericos);

        // Historial inicial
        equipo.getHistorial().add(nuevoEvento("Registro inicial del equipo", "Sistema", "GENERAL"));

        return equipoRepository.save(equipo);
    }

    // ── Actualizar datos básicos ──────────────────────────────────────────
    public Equipo actualizar(String codigo, EquipoRequestDTO dto) {
        Equipo equipo = buscarPorCodigo(codigo);
        equipo.setResponsable(dto.getResponsable());
        equipo.setCargo(dto.getCargo());
        equipo.setArea(dto.getArea());
        equipo.setTipoDispositivo(dto.getTipoDispositivo());
        equipo.setMarca(dto.getMarca());
        equipo.setModelo(dto.getModelo());
        equipo.setSerial(dto.getSerial());
        equipo.setMac(dto.getMac());
        equipo.setRam(dto.getRam());
        equipo.setDisco(dto.getDisco());
        equipo.setEstadoFisico(dto.getEstadoFisico());
        equipo.setEstado(dto.getEstado());
        equipo.setObservaciones(dto.getObservaciones());
        equipo.setEsPrestamo(dto.isEsPrestamo());
        equipo.setIncluyeCargador(dto.isIncluyeCargador());
        equipo.setFechaEntrega(dto.getFechaEntrega());
        equipo.setCargadorMarca(dto.getCargadorMarca());
        equipo.setCargadorModelo(dto.getCargadorModelo());
        equipo.setCargadorSerial(dto.getCargadorSerial());
        if (dto.getPerifericos() != null) {
            List<Periferico> perifericos = new ArrayList<>();
            for (PerifericoDTO p : dto.getPerifericos()) {
                if (p.getTipo() != null && !p.getTipo().isBlank()) {
                    Periferico peri = new Periferico();
                    peri.setTipo(p.getTipo());
                    peri.setMarca(p.getMarca());
                    peri.setModelo(p.getModelo());
                    peri.setSerial(p.getSerial());
                    perifericos.add(peri);
                }
            }
            equipo.setPerifericos(perifericos);
        }
        return equipoRepository.save(equipo);
    }

    // ── Cambiar propietario ───────────────────────────────────────────────
    public Equipo cambiarPropietario(String codigo, CambioPropietarioDTO dto) {
        Equipo equipo = buscarPorCodigo(codigo);
        String propietarioAnterior = equipo.getResponsable();
        String cargoAnterior = equipo.getCargo();

        equipo.setResponsable(dto.getNuevoResponsable());
        equipo.setCargo(dto.getNuevoCargo());
        if (dto.getNuevaArea() != null && !dto.getNuevaArea().isBlank()) {
            equipo.setArea(dto.getNuevaArea());
        }

        String descripcion = "Cambio de propietario: " + propietarioAnterior + " (" + cargoAnterior + ")"
                + " → " + dto.getNuevoResponsable() + " (" + dto.getNuevoCargo() + ")"
                + (dto.getMotivo() != null && !dto.getMotivo().isBlank() ? " | Motivo: " + dto.getMotivo() : "");
        String ing = dto.getIngeniero() != null && !dto.getIngeniero().isBlank() ? dto.getIngeniero() : "Sistema";
        equipo.getHistorial().add(nuevoEvento(descripcion, ing, "CAMBIO_PROPIETARIO"));

        return equipoRepository.save(equipo);
    }

    // ── Actualizar hardware ───────────────────────────────────────────────
    public Equipo actualizarHardware(String codigo, ActualizarHardwareDTO dto) {
        Equipo equipo = buscarPorCodigo(codigo);
        StringBuilder cambios = new StringBuilder("Actualización de hardware:");

        if (dto.getRam() != null && !dto.getRam().isBlank()) {
            cambios.append(" RAM: ").append(equipo.getRam()).append(" → ").append(dto.getRam()).append(";");
            equipo.setRam(dto.getRam());
        }
        if (dto.getDisco() != null && !dto.getDisco().isBlank()) {
            cambios.append(" Disco: ").append(equipo.getDisco()).append(" → ").append(dto.getDisco()).append(";");
            equipo.setDisco(dto.getDisco());
        }
        if (dto.getMac() != null && !dto.getMac().isBlank()) {
            cambios.append(" MAC: ").append(dto.getMac()).append(";");
            equipo.setMac(dto.getMac());
        }
        if (dto.getEstadoFisico() != null && !dto.getEstadoFisico().isBlank()) {
            cambios.append(" Estado físico: ").append(equipo.getEstadoFisico()).append(" → ").append(dto.getEstadoFisico()).append(";");
            equipo.setEstadoFisico(dto.getEstadoFisico());
        }
        if (dto.getDescripcion() != null && !dto.getDescripcion().isBlank()) {
            cambios.append(" Nota: ").append(dto.getDescripcion());
        }

        String ing = dto.getIngeniero() != null && !dto.getIngeniero().isBlank() ? dto.getIngeniero() : "Sistema";
        equipo.getHistorial().add(nuevoEvento(cambios.toString(), ing, "HARDWARE"));

        return equipoRepository.save(equipo);
    }

    // ── Estados operativos ────────────────────────────────────────────────
    public Equipo darDeBaja(String codigo) {
        Equipo equipo = buscarPorCodigo(codigo);
        equipo.setEstado("Dado de baja");
        equipo.getHistorial().add(nuevoEvento("Equipo dado de baja", "Sistema", "ESTADO"));
        return equipoRepository.save(equipo);
    }

    public Equipo ponerEnMantenimiento(String codigo) {
        Equipo equipo = buscarPorCodigo(codigo);
        equipo.setEstado("En mantenimiento");
        equipo.getHistorial().add(nuevoEvento("Equipo enviado a mantenimiento", "Sistema", "MANTENIMIENTO"));
        return equipoRepository.save(equipo);
    }

    public Equipo activarEquipo(String codigo) {
        Equipo equipo = buscarPorCodigo(codigo);
        equipo.setEstado("Activo");
        equipo.getHistorial().add(nuevoEvento("Equipo reactivado", "Sistema", "ESTADO"));
        return equipoRepository.save(equipo);
    }

    // ── Historial manual ──────────────────────────────────────────────────
    public Equipo agregarHistorial(String codigo, HistorialEventoDTO dto) {
        Equipo equipo = buscarPorCodigo(codigo);
        String tipo = dto.getTipo() != null ? dto.getTipo() : "GENERAL";
        equipo.getHistorial().add(nuevoEvento(dto.getEvento(), dto.getIngeniero(), tipo));
        return equipoRepository.save(equipo);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────
    public void eliminar(String codigo) {
        equipoRepository.delete(buscarPorCodigo(codigo));
    }

    // ── QR ───────────────────────────────────────────────────────────────
    public String obtenerQRBase64(String codigo) throws WriterException, IOException {
        return qrGenerador.generarBase64(codigo);
    }

    public byte[] obtenerQRBytes(String codigo) throws WriterException, IOException {
        return qrGenerador.generarBytes(codigo);
    }

    // ── Helper ───────────────────────────────────────────────────────────
    private HistorialEvento nuevoEvento(String evento, String ingeniero, String tipo) {
        HistorialEvento ev = new HistorialEvento();
        ev.setFecha(LocalDate.now());
        ev.setEvento(evento);
        ev.setIngeniero(ingeniero != null ? ingeniero : "Sistema");
        ev.setTipo(tipo);
        return ev;
    }
}