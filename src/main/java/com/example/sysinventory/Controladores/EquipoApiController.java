package com.example.sysinventory.Controladores;

import com.example.sysinventory.DTOs.EquipoRequestDTO;
import com.example.sysinventory.DTOs.HistorialEventoDTO;
import com.example.sysinventory.Modelos.Equipo;
import com.example.sysinventory.Servicios.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EquipoApiController {

    private final EquipoService equipoService;

    public EquipoApiController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping("/debug/equipos")
    public List<Map<String, Object>> debug() {
        return equipoService.listarTodos().stream().map(e -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("codigo", e.getCodigo());
            m.put("area", e.getArea());
            m.put("estado", e.getEstado());
            m.put("responsable", e.getResponsable());
            return m;
        }).toList();
    }

    @GetMapping("/equipos")
    public List<Equipo> listar() {
        return equipoService.listarTodos();
    }

    @GetMapping("/equipos/{codigo}")
    public Equipo obtener(@PathVariable String codigo) {
        return equipoService.buscarPorCodigo(codigo);
    }

    @PostMapping("/equipos")
    public Equipo crear(@Valid @RequestBody EquipoRequestDTO dto) {
        return equipoService.crear(dto);
    }

    @PutMapping("/equipos/{codigo}")
    public Equipo actualizar(@PathVariable String codigo,
                             @Valid @RequestBody EquipoRequestDTO dto) {
        return equipoService.actualizar(codigo, dto);
    }

    @DeleteMapping("/equipos/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        equipoService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/equipos/{codigo}/historial")
    public Equipo agregarHistorial(@PathVariable String codigo,
                                   @Valid @RequestBody HistorialEventoDTO dto) {
        return equipoService.agregarHistorial(codigo, dto);
    }

    @GetMapping("/equipos/{codigo}/qr")
    public ResponseEntity<byte[]> descargarQR(@PathVariable String codigo) throws Exception {
        byte[] qrBytes = equipoService.obtenerQRBytes(codigo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + codigo + "-QR.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}