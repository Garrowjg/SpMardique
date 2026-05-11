package com.example.sysinventory.Utilidades;

import com.example.sysinventory.Repositorio.EquipoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CodigoGenerador {

    private final EquipoRepository equipoRepository;

    public CodigoGenerador(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    // Áreas → abreviatura
    private static final Map<String, String> AREAS = Map.of(
            "Contabilidad",    "CONTAB",
            "Sistemas",        "SISTEM",
            "Gerencia",        "GERENC",
            "Recursos Humanos","RRHH",
            "Comercial",       "COMERC",
            "Logística",       "LOGIST"
    );

    // Cargos → abreviatura + prioridad jerárquica (menor número = mayor jerarquía)
    private static final Map<String, String> CARGOS = Map.of(
            "Gerente",     "GEREN",
            "Coordinador", "COORD",
            "Analista",    "ANALI",
            "Técnico",     "TECNI",
            "Auxiliar",    "AUXIL",
            "Asesor",      "ASESOR"
    );

    // Orden jerárquico de cargos (posición = número base asignado)
    private static final List<String> JERARQUIA_CARGOS = List.of(
            "Gerente",      // → 01
            "Coordinador",  // → 02
            "Analista",     // → 03
            "Técnico",      // → 04
            "Auxiliar",     // → 05
            "Asesor"        // → 06
    );

    private static final Map<String, List<String>> MARCAS_MODELOS = Map.of(
            "HP", List.of(
                    "ProBook 440 G8", "ProBook 450 G9", "EliteBook 840 G8",
                    "EliteBook 850 G9", "HP 240 G8", "HP ProDesk 400 G7",
                    "HP EliteDesk 800 G6", "HP Z2 Tower G9"
            ),
            "Dell", List.of(
                    "Latitude 5420", "Latitude 5520", "Latitude 7420",
                    "Inspiron 15 3000", "OptiPlex 3090", "OptiPlex 5090",
                    "Vostro 3510", "Precision 3560"
            ),
            "Lenovo", List.of(
                    "ThinkPad E14 Gen 4", "ThinkPad L14 Gen 3", "ThinkPad T14 Gen 3",
                    "IdeaPad 3 15ITL6", "ThinkCentre M70q", "ThinkCentre M90n",
                    "Legion 5 Gen 7", "ThinkPad X1 Carbon Gen 10"
            ),
            "Asus", List.of(
                    "ExpertBook B1 B1500", "ExpertBook B5 B5402",
                    "VivoBook 15 X1502", "ProArt StudioBook 16", "Mini PC PN53"
            ),
            "Acer", List.of(
                    "Aspire 5 A515-57", "TravelMate P2 TMP215",
                    "Veriton S2690G", "Nitro 5 AN515-58", "Swift 3 SF314-54"
            ),
            "Apple", List.of(
                    "MacBook Air M1", "MacBook Air M2", "MacBook Pro 14 M2",
                    "MacBook Pro 16 M2", "Mac Mini M2", "iMac 24 M1"
            ),
            "MSI", List.of(
                    "Modern 14 B11MOU", "Modern 15 A11MU", "Summit B14 A11M",
                    "Prestige 14 A12UC"
            ),
            "Toshiba / Dynabook", List.of(
                    "Tecra A50-J", "Satellite Pro C50", "Portégé X30L-J"
            )
    );

    private static final List<String> OPCIONES_RAM = List.of(
            "4 GB DDR4", "8 GB DDR4", "16 GB DDR4", "32 GB DDR4", "64 GB DDR4",
            "8 GB DDR5", "16 GB DDR5", "32 GB DDR5"
    );

    private static final List<String> OPCIONES_DISCO = List.of(
            "128 GB SSD", "256 GB SSD", "512 GB SSD", "1 TB SSD", "2 TB SSD",
            "500 GB HDD", "1 TB HDD", "2 TB HDD", "256 GB SSD + 1 TB HDD"
    );

    /**
     * Genera código con numeración jerárquica por cargo dentro del área.
     * Ejemplo: RRHH-GEREN-01, RRHH-COORD-01, RRHH-COORD-02
     * El número refleja cuántos equipos del mismo cargo+área ya existen + 1.
     */
    public String generar(String area, String cargo) {
        String abvArea  = AREAS.getOrDefault(area, area.toUpperCase().substring(0, Math.min(6, area.length())));
        String abvCargo = CARGOS.getOrDefault(cargo, cargo.toUpperCase().substring(0, Math.min(6, cargo.length())));
        String prefijo  = abvArea + "-" + abvCargo + "-";

        int numero = 1;
        while (equipoRepository.existsByCodigo(prefijo + String.format("%02d", numero))) {
            numero++;
        }
        return prefijo + String.format("%02d", numero);
    }

    public static Map<String, String> getAreas()               { return AREAS; }
    public static Map<String, String> getCargos()              { return CARGOS; }
    public static Map<String, List<String>> getMarcasModelos() { return MARCAS_MODELOS; }
    public static List<String> getOpcionesRam()                { return OPCIONES_RAM; }
    public static List<String> getOpcionesDisco()              { return OPCIONES_DISCO; }
    public static List<String> getJerarquiaCargos()            { return JERARQUIA_CARGOS; }
}
