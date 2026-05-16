package com.example.sysinventory.Utilidades;

import com.example.sysinventory.Repositorio.EquipoRepository;
import com.example.sysinventory.Servicios.CatalogoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CodigoGenerador {

    private final EquipoRepository equipoRepository;
    private final CatalogoService  catalogoService;

    public CodigoGenerador(EquipoRepository equipoRepository,
                           CatalogoService catalogoService) {
        this.equipoRepository = equipoRepository;
        this.catalogoService  = catalogoService;
    }

    // ── Generación de código ───────────────────────────────────────────────────

    public String generar(String area, String cargo) {
        String abvArea  = catalogoService.abrevArea(area);
        String abvCargo = catalogoService.abrevCargo(cargo);
        long count      = equipoRepository.countByArea(area);
        return abvArea + "-" + abvCargo + "-" + String.format("%02d", count + 1);
    }

    public String regenerarCodigo(String area, String cargo) {
        return generar(area, cargo);
    }
}