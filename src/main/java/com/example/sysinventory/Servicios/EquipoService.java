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
import java.util.stream.Collectors;

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

    public List<Equipo> listarTodos() {
        List<Equipo> lista = equipoRepository.findAll();
        return lista != null ? lista : new ArrayList<>();
    }

    public Equipo buscarPorCodigo(String codigo) {
        return equipoRepository.findByCodigo(codigo).orElse(null);
    }

    public Equipo buscarPorCodigoOLanzar(String codigo) {
        return equipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + codigo));
    }

    public List<Equipo> buscarPorArea(String area) {
        return equipoRepository.findByArea(area);
    }

    public List<Equipo> buscarPorEstado(String estado) {
        return equipoRepository.findByEstado(estado);
    }

    public Equipo crear(EquipoRequestDTO dto) {
        // Verificar si ya existe un equipo con la misma área y cargo
        List<Equipo> existentes = equipoRepository.findByAreaAndCargo(dto.getArea(), dto.getCargo());
        if (!existentes.isEmpty()) {
            throw new RuntimeException("Ya existe un equipo asignado al área " + dto.getArea() + " con el cargo " + dto.getCargo() + ". No se puede duplicar.");
        }

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
        equipo.setProcesador(dto.getProcesador());
        equipo.setEstadoFisico(dto.getEstadoFisico());
        equipo.setEstado(dto.getEstado() != null && !dto.getEstado().isBlank() ? dto.getEstado() : "Activo");
        equipo.setObservaciones(dto.getObservaciones());
        equipo.setIncluyeCargador(dto.isIncluyeCargador());
        equipo.setCargadorMarca(dto.getCargadorMarca());
        equipo.setCargadorModelo(dto.getCargadorModelo());
        equipo.setCargadorSerial(dto.getCargadorSerial());

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

        equipo.getHistorial().add(nuevoEvento("Registro inicial del equipo", "Sistema", "GENERAL"));

        return equipoRepository.save(equipo);
    }

    public Equipo actualizar(String codigo, EquipoRequestDTO dto) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
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
        equipo.setProcesador(dto.getProcesador());
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

    public Equipo cambiarPropietario(String codigo, CambioPropietarioDTO dto) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        String propietarioAnterior = equipo.getResponsable() != null ? equipo.getResponsable() : "Sin asignar";
        String cargoAnterior       = equipo.getCargo()       != null ? equipo.getCargo()       : "Sin asignar";
        String areaAnterior        = equipo.getArea()        != null ? equipo.getArea()        : "Sin asignar";

        // Equipo venía de stock/baja: area y cargo son null → SIEMPRE hay cambio
        boolean estabaEnStockOBaja = equipo.getArea() == null || equipo.getCargo() == null;

        boolean areaCambio  = estabaEnStockOBaja ||
                (dto.getNuevaArea()  != null && !dto.getNuevaArea().isBlank()  && !dto.getNuevaArea().equals(equipo.getArea()));
        boolean cargoCambio = estabaEnStockOBaja ||
                (dto.getNuevoCargo() != null && !dto.getNuevoCargo().isBlank() && !dto.getNuevoCargo().equals(equipo.getCargo()));

        equipo.setResponsable(dto.getNuevoResponsable());
        equipo.setCargo(dto.getNuevoCargo() != null && !dto.getNuevoCargo().isBlank() ? dto.getNuevoCargo() : equipo.getCargo());

        String nuevaArea = (dto.getNuevaArea() != null && !dto.getNuevaArea().isBlank())
                ? dto.getNuevaArea()
                : equipo.getArea();
        equipo.setArea(nuevaArea);

        // Regenerar código si cambiaron área o cargo (incluye reactivación desde stock/baja)
        if (areaCambio || cargoCambio) {
            // Validar que el área y cargo nuevos no estén ya ocupados por OTRO equipo activo
            String areaFinal  = equipo.getArea();
            String cargoFinal = equipo.getCargo();
            if (areaFinal != null && cargoFinal != null) {
                List<Equipo> existentes = equipoRepository.findByAreaAndCargo(areaFinal, cargoFinal);
                // Filtrar el propio equipo de la lista
                existentes = existentes.stream()
                        .filter(e -> !e.getId().equals(equipo.getId()))
                        .collect(java.util.stream.Collectors.toList());
                if (!existentes.isEmpty()) {
                    throw new RuntimeException("Ya existe un equipo activo en el área '"
                            + areaFinal + "' con el cargo '" + cargoFinal
                            + "'. Libera ese puesto antes de reasignar.");
                }
                String nuevoCodigo = codigoGenerador.generar(areaFinal, cargoFinal);
                equipo.setCodigo(nuevoCodigo);
            }
        }

        String descripcion = "Cambio de propietario: " + propietarioAnterior + " (" + cargoAnterior + ")"
                + " → " + dto.getNuevoResponsable() + " (" + equipo.getCargo() + ")"
                + (dto.getMotivo() != null && !dto.getMotivo().isBlank() ? " | Motivo: " + dto.getMotivo() : "");
        if (!areaAnterior.equals(equipo.getArea() != null ? equipo.getArea() : "Sin asignar")) {
            descripcion += " | Área: " + areaAnterior + " → " + equipo.getArea();
        }

        String ing = dto.getIngeniero() != null && !dto.getIngeniero().isBlank() ? dto.getIngeniero() : "Sistema";
        equipo.getHistorial().add(nuevoEvento(descripcion, ing, "CAMBIO_PROPIETARIO"));

        return equipoRepository.save(equipo);
    }

    public Equipo actualizarHardware(String codigo, ActualizarHardwareDTO dto) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        StringBuilder cambios = new StringBuilder("Actualización de hardware:");

        if (dto.getRam() != null && !dto.getRam().isBlank()) {
            cambios.append(" RAM: ").append(equipo.getRam()).append(" → ").append(dto.getRam()).append(";");
            equipo.setRam(dto.getRam());
        }
        if (dto.getDisco() != null && !dto.getDisco().isBlank()) {
            cambios.append(" Disco: ").append(equipo.getDisco()).append(" → ").append(dto.getDisco()).append(";");
            equipo.setDisco(dto.getDisco());
        }
        if (dto.getProcesador() != null && !dto.getProcesador().isBlank()) {
            cambios.append(" Procesador: ").append(equipo.getProcesador()).append(" → ").append(dto.getProcesador()).append(";");
            equipo.setProcesador(dto.getProcesador());
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

    public Equipo ponerEnMantenimiento(String codigo, String motivo) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        equipo.setEstado("En mantenimiento");
        String evento = "Equipo enviado a mantenimiento. Responsable anterior: " + (equipo.getResponsable() != null ? equipo.getResponsable() : "Desconocido")
                + ", Área: " + (equipo.getArea() != null ? equipo.getArea() : "Sin asignar")
                + (motivo != null && !motivo.isBlank() ? " | Motivo: " + motivo : "");
        equipo.getHistorial().add(nuevoEvento(evento, "Sistema", "MANTENIMIENTO"));
        return equipoRepository.save(equipo);
    }

    public Equipo activarEquipo(String codigo) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        equipo.setEstado("Activo");
        equipo.getHistorial().add(nuevoEvento("Equipo reactivado", "Sistema", "ESTADO"));
        return equipoRepository.save(equipo);
    }

    public Equipo ponerEnStock(String codigo, String motivo) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        String responsableAnterior = equipo.getResponsable() != null ? equipo.getResponsable() : "Desconocido";
        String areaAnterior = equipo.getArea() != null ? equipo.getArea() : "Sin asignar";
        String cargoAnterior = equipo.getCargo() != null ? equipo.getCargo() : "Sin asignar";

        String nuevoCodigo = generarCodigoEspecial("STOCK");
        equipo.setCodigo(nuevoCodigo);

        equipo.setArea(null);
        equipo.setCargo(null);
        equipo.setResponsable(null);
        equipo.setFechaEntrega(null);
        equipo.setEsPrestamo(false);
        equipo.setObservaciones("Equipo en stock - " + (equipo.getObservaciones() != null ? equipo.getObservaciones() : ""));
        equipo.setEstado("En stock");

        String evento = "Equipo puesto en stock. Código anterior: " + codigo
                + ". Responsable anterior: " + responsableAnterior
                + ", Área: " + areaAnterior + ", Cargo: " + cargoAnterior
                + (motivo != null && !motivo.isBlank() ? " | Motivo: " + motivo : "");
        equipo.getHistorial().add(nuevoEvento(evento, "Sistema", "ESTADO"));

        return equipoRepository.save(equipo);
    }

    public Equipo darDeBaja(String codigo, String motivo) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        String responsableAnterior = equipo.getResponsable() != null ? equipo.getResponsable() : "Desconocido";
        String areaAnterior = equipo.getArea() != null ? equipo.getArea() : "Sin asignar";
        String cargoAnterior = equipo.getCargo() != null ? equipo.getCargo() : "Sin asignar";

        String nuevoCodigo = generarCodigoEspecial("BAJA");
        equipo.setCodigo(nuevoCodigo);

        equipo.setArea(null);
        equipo.setCargo(null);
        equipo.setResponsable(null);
        equipo.setFechaEntrega(null);
        equipo.setEsPrestamo(false);
        equipo.setObservaciones("Equipo dado de baja - " + (equipo.getObservaciones() != null ? equipo.getObservaciones() : ""));
        equipo.setEstado("Dado de baja");

        String evento = "Equipo dado de baja. Código anterior: " + codigo
                + ". Responsable anterior: " + responsableAnterior
                + ", Área: " + areaAnterior + ", Cargo: " + cargoAnterior
                + (motivo != null && !motivo.isBlank() ? " | Motivo: " + motivo : "");
        equipo.getHistorial().add(nuevoEvento(evento, "Sistema", "ESTADO"));

        return equipoRepository.save(equipo);
    }

    public Equipo agregarHistorial(String codigo, HistorialEventoDTO dto) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        String tipo = dto.getTipo() != null ? dto.getTipo() : "GENERAL";
        equipo.getHistorial().add(nuevoEvento(dto.getEvento(), dto.getIngeniero(), tipo));
        return equipoRepository.save(equipo);
    }

    public void eliminar(String codigo) {
        equipoRepository.delete(buscarPorCodigoOLanzar(codigo));
    }

    public String obtenerQRBase64(String codigo) throws WriterException, IOException {
        return qrGenerador.generarBase64(codigo);
    }

    public byte[] obtenerQRBytes(String codigo) throws WriterException, IOException {
        return qrGenerador.generarBytes(codigo);
    }

    public Equipo eliminarPeriferico(String codigo, int index, String motivo) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);
        if (index >= 0 && index < equipo.getPerifericos().size()) {
            Periferico eliminado = equipo.getPerifericos().remove(index);
            String descripcion = "Periférico eliminado: " + eliminado.getTipo()
                    + " (Marca: " + (eliminado.getMarca() != null ? eliminado.getMarca() : "N/A")
                    + ", Serial: " + (eliminado.getSerial() != null ? eliminado.getSerial() : "N/A") + ")"
                    + (motivo != null && !motivo.isBlank() ? " | Motivo: " + motivo : "");
            equipo.getHistorial().add(nuevoEvento(descripcion, "Sistema", "GENERAL"));
            return equipoRepository.save(equipo);
        } else {
            throw new RuntimeException("Índice de periférico inválido");
        }
    }

    // ================= NUEVO: AGREGAR PERIFÉRICO =================
    public Equipo agregarPeriferico(String codigo, String tipo, String marca, String modelo, String serial) {
        Equipo equipo = buscarPorCodigoOLanzar(codigo);

        if (equipo.getPerifericos() == null) {
            equipo.setPerifericos(new ArrayList<>());
        }

        Periferico peri = new Periferico();
        peri.setTipo(tipo);
        peri.setMarca(marca != null && !marca.isBlank() ? marca : null);
        peri.setModelo(modelo != null && !modelo.isBlank() ? modelo : null);
        peri.setSerial(serial != null && !serial.isBlank() ? serial : null);

        equipo.getPerifericos().add(peri);

        String descripcion = "Periférico agregado: " + tipo
                + (marca != null && !marca.isBlank() ? " " + marca : "")
                + (modelo != null && !modelo.isBlank() ? " " + modelo : "")
                + (serial != null && !serial.isBlank() ? " (S/N: " + serial + ")" : "");
        equipo.getHistorial().add(nuevoEvento(descripcion, "Sistema", "GENERAL"));

        return equipoRepository.save(equipo);
    }

    // ================= PRIVADOS =================

    private HistorialEvento nuevoEvento(String evento, String ingeniero, String tipo) {
        HistorialEvento ev = new HistorialEvento();
        ev.setFecha(LocalDate.now());
        ev.setEvento(evento);
        ev.setIngeniero(ingeniero != null ? ingeniero : "Sistema");
        ev.setTipo(tipo);
        return ev;
    }

    private String generarCodigoEspecial(String prefijo) {
        List<Equipo> todos = equipoRepository.findAll();
        int maxNum = 0;
        for (Equipo e : todos) {
            if (e.getCodigo() != null && e.getCodigo().startsWith(prefijo + "-")) {
                String numStr = e.getCodigo().substring((prefijo + "-").length());
                try {
                    int num = Integer.parseInt(numStr);
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        int next = maxNum + 1;
        return String.format("%s-%03d", prefijo, next);
    }
}