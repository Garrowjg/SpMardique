package com.example.sysinventory.Servicios;

import com.example.sysinventory.Modelos.Catalogo;
import com.example.sysinventory.Repositorio.CatalogoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CatalogoService {

    private final CatalogoRepository catalogoRepository;

    // ── Datos base de Mardique SP ──────────────────────────────────────────────

    private static final List<String> AREAS_DEFAULT = List.of(
            "Control de Acceso - Seguridad",
            "TI",
            "Oficios Varios",
            "HSQ",
            "Contabilidad",
            "Talento Humano",
            "Compras",
            "Documentación y Básculas",
            "Operaciones",
            "Zona Franca",
            "Seguridad",
            "SSTV"
    );

    private static final List<String> CARGOS_DEFAULT = List.of(
            "Analista",
            "Auxiliar",
            "Coordinador",
            "Supervisor",
            "Representante Legal",
            "Gerente",
            "Presidente"
    );

    private static final List<String> RAM_DEFAULT = List.of(
            "4 GB DDR4", "8 GB DDR4", "16 GB DDR4", "32 GB DDR4", "64 GB DDR4",
            "8 GB DDR5", "16 GB DDR5", "32 GB DDR5"
    );

    private static final List<String> DISCO_DEFAULT = List.of(
            "128 GB SSD", "256 GB SSD", "512 GB SSD", "1 TB SSD", "2 TB SSD",
            "500 GB HDD", "1 TB HDD", "2 TB HDD", "256 GB SSD + 1 TB HDD"
    );

    private static final List<String> MARCAS_DEFAULT = List.of(
            "HP", "Dell", "Lenovo", "Asus", "Acer", "Apple", "MSI", "Toshiba / Dynabook"
    );

    private static final Map<String, List<String>> MODELOS_DEFAULT = Map.of(
            "HP", List.of("ProBook 440 G8","ProBook 450 G9","EliteBook 840 G8",
                    "EliteBook 850 G9","HP 240 G8","HP ProDesk 400 G7",
                    "HP EliteDesk 800 G6","HP Z2 Tower G9"),
            "Dell", List.of("Latitude 5420","Latitude 5520","Latitude 7420",
                    "Inspiron 15 3000","OptiPlex 3090","OptiPlex 5090",
                    "Vostro 3510","Precision 3560"),
            "Lenovo", List.of("ThinkPad E14 Gen 4","ThinkPad L14 Gen 3","ThinkPad T14 Gen 3",
                    "IdeaPad 3 15ITL6","ThinkCentre M70q","ThinkCentre M90n",
                    "Legion 5 Gen 7","ThinkPad X1 Carbon Gen 10"),
            "Asus", List.of("ExpertBook B1 B1500","ExpertBook B5 B5402",
                    "VivoBook 15 X1502","ProArt StudioBook 16","Mini PC PN53"),
            "Acer", List.of("Aspire 5 A515-57","TravelMate P2 TMP215",
                    "Veriton S2690G","Nitro 5 AN515-58","Swift 3 SF314-54"),
            "Apple", List.of("MacBook Air M1","MacBook Air M2","MacBook Pro 14 M2",
                    "MacBook Pro 16 M2","Mac Mini M2","iMac 24 M1"),
            "MSI", List.of("Modern 14 B11MOU","Modern 15 A11MU",
                    "Summit B14 A11M","Prestige 14 A12UC"),
            "Toshiba / Dynabook", List.of("Tecra A50-J","Satellite Pro C50","Portégé X30L-J")
    );

    // ── Constructor ────────────────────────────────────────────────────────────

    public CatalogoService(CatalogoRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    // ── Inicialización: si la BD está vacía, inserta los datos base ───────────

    @PostConstruct
    public void inicializar() {
        seedSiNoExiste("areas",        AREAS_DEFAULT);
        seedSiNoExiste("cargos",       CARGOS_DEFAULT);
        seedSiNoExiste("opciones_ram", RAM_DEFAULT);
        seedSiNoExiste("opciones_disco", DISCO_DEFAULT);
        seedSiNoExiste("marcas",       MARCAS_DEFAULT);
        for (Map.Entry<String, List<String>> entry : MODELOS_DEFAULT.entrySet()) {
            seedSiNoExiste("modelos_" + entry.getKey(), entry.getValue());
        }
    }

    private void seedSiNoExiste(String tipo, List<String> valores) {
        if (!catalogoRepository.existsByTipo(tipo)) {
            catalogoRepository.save(new Catalogo(tipo, new ArrayList<>(valores)));
        }
    }

    // ── Lectura ────────────────────────────────────────────────────────────────

    public List<String> getAreas() {
        return getValores("areas");
    }

    public List<String> getCargos() {
        return getValores("cargos");
    }

    public List<String> getOpcionesRam() {
        return getValores("opciones_ram");
    }

    public List<String> getOpcionesDisco() {
        return getValores("opciones_disco");
    }

    public List<String> getMarcas() {
        return getValores("marcas");
    }

    public List<String> getModelosPorMarca(String marca) {
        return getValores("modelos_" + marca);
    }

    /** Devuelve Map<marca, List<modelos>> para el formulario */
    public Map<String, List<String>> getMarcasModelos() {
        List<String> marcas = getMarcas();
        Map<String, List<String>> resultado = new LinkedHashMap<>();
        for (String marca : marcas) {
            resultado.put(marca, getModelosPorMarca(marca));
        }
        return resultado;
    }

    private List<String> getValores(String tipo) {
        return catalogoRepository.findByTipo(tipo)
                .map(Catalogo::getValores)
                .orElse(new ArrayList<>());
    }

    // ── Escritura ──────────────────────────────────────────────────────────────

    public void agregarArea(String nueva) {
        agregarValor("areas", nueva);
    }

    public void agregarCargo(String nuevo) {
        agregarValor("cargos", nuevo);
    }

    public void agregarRam(String nueva) {
        agregarValor("opciones_ram", nueva);
    }

    public void agregarDisco(String nuevo) {
        agregarValor("opciones_disco", nuevo);
    }

    public void agregarMarca(String nueva) {
        agregarValor("marcas", nueva);
        // Crea el documento de modelos vacío para la nueva marca
        String tipoModelos = "modelos_" + nueva;
        if (!catalogoRepository.existsByTipo(tipoModelos)) {
            catalogoRepository.save(new Catalogo(tipoModelos, new ArrayList<>()));
        }
    }

    public void agregarModelo(String marca, String nuevoModelo) {
        agregarValor("modelos_" + marca, nuevoModelo);
    }

    private void agregarValor(String tipo, String valor) {
        if (valor == null || valor.isBlank()) return;
        Catalogo catalogo = catalogoRepository.findByTipo(tipo)
                .orElseGet(() -> new Catalogo(tipo, new ArrayList<>()));
        if (!catalogo.getValores().contains(valor)) {
            catalogo.getValores().add(valor);
            catalogoRepository.save(catalogo);
        }
    }

    // ── Abreviaciones para generar códigos ────────────────────────────────────

    private static final Map<String, String> ABREV_AREAS = Map.ofEntries(
            Map.entry("Control de Acceso - Seguridad", "CTLACC"),
            Map.entry("TI",                            "TI"),
            Map.entry("Oficios Varios",                "OFVARI"),
            Map.entry("HSQ",                           "HSQ"),
            Map.entry("Contabilidad",                  "CONTAB"),
            Map.entry("Talento Humano",                "TALHU"),
            Map.entry("Compras",                       "COMPRA"),
            Map.entry("Documentación y Básculas",      "DOCBAS"),
            Map.entry("Operaciones",                   "OPERAD"),
            Map.entry("Zona Franca",                   "ZFRCA"),
            Map.entry("Seguridad",                     "SEGURI"),
            Map.entry("SSTV",                          "SSTV")
    );

    private static final Map<String, String> ABREV_CARGOS = Map.of(
            "Analista",           "ANALI",
            "Auxiliar",           "AUXIL",
            "Coordinador",        "COORD",
            "Supervisor",         "SUPER",
            "Representante Legal","REPLE",
            "Gerente",            "GEREN",
            "Presidente",         "PRESI"
    );

    public String abrevArea(String area) {
        return ABREV_AREAS.getOrDefault(area,
                area.toUpperCase().replaceAll("[^A-Z0-9]", "").substring(0, Math.min(6, area.toUpperCase().replaceAll("[^A-Z0-9]", "").length())));
    }

    public String abrevCargo(String cargo) {
        return ABREV_CARGOS.getOrDefault(cargo,
                cargo.toUpperCase().replaceAll("[^A-Z0-9]", "").substring(0, Math.min(6, cargo.toUpperCase().replaceAll("[^A-Z0-9]", "").length())));
    }
}