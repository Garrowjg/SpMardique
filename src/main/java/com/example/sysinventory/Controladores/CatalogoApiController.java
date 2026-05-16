package com.example.sysinventory.Controladores;

import com.example.sysinventory.Servicios.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints REST para gestionar catálogos dinámicos (áreas, cargos, RAM, disco, marcas, modelos).
 * Todos los valores nuevos se persisten en MongoDB (colección 'catalogos').
 */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoApiController {

    private final CatalogoService catalogoService;

    public CatalogoApiController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // ── GET: leer listas completas ─────────────────────────────────────────────

    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAreas() {
        return ResponseEntity.ok(catalogoService.getAreas());
    }

    @GetMapping("/cargos")
    public ResponseEntity<List<String>> getCargos() {
        return ResponseEntity.ok(catalogoService.getCargos());
    }

    @GetMapping("/ram")
    public ResponseEntity<List<String>> getRam() {
        return ResponseEntity.ok(catalogoService.getOpcionesRam());
    }

    @GetMapping("/disco")
    public ResponseEntity<List<String>> getDisco() {
        return ResponseEntity.ok(catalogoService.getOpcionesDisco());
    }

    @GetMapping("/marcas")
    public ResponseEntity<List<String>> getMarcas() {
        return ResponseEntity.ok(catalogoService.getMarcas());
    }

    @GetMapping("/modelos/{marca}")
    public ResponseEntity<List<String>> getModelos(@PathVariable String marca) {
        return ResponseEntity.ok(catalogoService.getModelosPorMarca(marca));
    }

    // ── POST: agregar nuevo valor ──────────────────────────────────────────────

    @PostMapping("/areas")
    public ResponseEntity<Map<String, Object>> agregarArea(@RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarArea(valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }

    @PostMapping("/cargos")
    public ResponseEntity<Map<String, Object>> agregarCargo(@RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarCargo(valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }

    @PostMapping("/ram")
    public ResponseEntity<Map<String, Object>> agregarRam(@RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarRam(valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }

    @PostMapping("/disco")
    public ResponseEntity<Map<String, Object>> agregarDisco(@RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarDisco(valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }

    @PostMapping("/marcas")
    public ResponseEntity<Map<String, Object>> agregarMarca(@RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarMarca(valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }

    @PostMapping("/modelos/{marca}")
    public ResponseEntity<Map<String, Object>> agregarModelo(@PathVariable String marca,
                                                             @RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null || valor.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Valor vacío"));
        catalogoService.agregarModelo(marca, valor.trim());
        return ResponseEntity.ok(Map.of("ok", true, "valor", valor.trim()));
    }
}