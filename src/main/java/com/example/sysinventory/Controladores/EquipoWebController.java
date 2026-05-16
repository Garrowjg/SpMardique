package com.example.sysinventory.Controladores;

import com.example.sysinventory.DTOs.*;
import com.example.sysinventory.Modelos.Equipo;
import com.example.sysinventory.Servicios.CatalogoService;
import com.example.sysinventory.Servicios.EquipoService;
import com.example.sysinventory.Servicios.GraphService;
import com.example.sysinventory.Servicios.TokenService;
import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EquipoWebController {

    private final EquipoService   equipoService;
    private final GraphService    graphService;
    private final TokenService    tokenService;
    private final CatalogoService catalogoService;

    @Value("${azure.inventario-download-url}")
    private String inventarioDownloadUrl;

    @Value("${formato.acta-entrega.url}")
    private String actaEntregaUrl;
    @Value("${formato.informe-mantenimiento.url}")
    private String informeMantenimientoUrl;
    @Value("${formato.acta-baja.url}")
    private String actaBajaUrl;

    @Value("${formato.acta-entrega.download}")
    private String actaEntregaDownload;
    @Value("${formato.informe-mantenimiento.download}")
    private String informeMantenimientoDownload;
    @Value("${formato.acta-baja.download}")
    private String actaBajaDownload;

    public EquipoWebController(EquipoService equipoService,
                               GraphService graphService,
                               TokenService tokenService,
                               CatalogoService catalogoService) {
        this.equipoService   = equipoService;
        this.graphService    = graphService;
        this.tokenService    = tokenService;
        this.catalogoService = catalogoService;
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private void agregarDatosUsuario(Model model) {
        String userName  = "Usuario";
        String userEmail = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) principal;
                String nombre = oauth2User.getAttribute("name");
                if (nombre == null || nombre.isBlank()) {
                    nombre = oauth2User.getAttribute("given_name");
                    String apellido = oauth2User.getAttribute("family_name");
                    if (nombre != null && apellido != null) nombre = nombre + " " + apellido;
                }
                if (nombre == null || nombre.isBlank())
                    nombre = oauth2User.getAttribute("preferred_username");
                userName  = (nombre != null && !nombre.isBlank()) ? nombre : "Usuario";
                userEmail = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email") : "";
            } else {
                userName = auth.getName();
            }
        }
        model.addAttribute("userName",  userName);
        model.addAttribute("userEmail", userEmail);
    }

    /** Agrega al modelo todas las listas necesarias para el formulario */
    private void agregarCatalogos(Model model) {
        model.addAttribute("areas",         catalogoService.getAreas());
        model.addAttribute("cargos",        catalogoService.getCargos());
        model.addAttribute("marcasModelos", catalogoService.getMarcasModelos());
        model.addAttribute("opcionesRam",   catalogoService.getOpcionesRam());
        model.addAttribute("opcionesDisco", catalogoService.getOpcionesDisco());
    }

    // ── Redirecciones ─────────────────────────────────────────────────────────

    @GetMapping("/redirect/{codigo}")
    public String redirigirAExcel(@PathVariable String codigo) {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        if (equipo == null) return "redirect:/equipos";
        return "redirect:/equipo/" + codigo + "/hoja";
    }

    @GetMapping("/equipo/{codigo}/hoja")
    public String hojaVida(@PathVariable String codigo,
                           HttpSession session, Model model) {
        agregarDatosUsuario(model);
        String token = tokenService.getAccessToken();
        if (token == null) {
            token = (String) session.getAttribute("accessToken");
            if (token == null) return "redirect:/auth/login";
            tokenService.setAccessToken(token);
        }
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        if (equipo == null) return "redirect:/equipos";
        String serial   = equipo.getSerial() != null ? equipo.getSerial().trim() : "";
        String embedUrl = serial.isBlank() ? "" : graphService.obtenerEmbedUrl(serial, token);
        model.addAttribute("equipo",        equipo);
        model.addAttribute("excelEmbedUrl", embedUrl);
        return "equipos/hoja";
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Equipo> equipos = equipoService.listarTodos();

        long activos       = equipos.stream().filter(e -> "Activo".equals(e.getEstado())).count();
        long mantenimiento = equipos.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
        long baja          = equipos.stream().filter(e -> "Dado de baja".equals(e.getEstado())).count();
        long stock         = equipos.stream().filter(e -> "En stock".equals(e.getEstado())).count();
        long totalEquipos  = equipos.size();
        long incidentes    = mantenimiento + baja;
        long areasActivas  = equipos.stream()
                .filter(e -> "Activo".equals(e.getEstado()) && e.getArea() != null && !e.getArea().isBlank())
                .map(Equipo::getArea).distinct().count();
        int disponibilidad = totalEquipos > 0 ? (int)(activos * 100 / totalEquipos) : 0;
        String rendimientoTexto = disponibilidad >= 90 ? "Óptimo" :
                disponibilidad >= 70 ? "Bueno" :
                disponibilidad >= 50 ? "Regular" : "Crítico";

        Map<String, List<Equipo>> porArea = equipos.stream()
                .filter(e -> e.getArea() != null && !e.getArea().isBlank())
                .collect(Collectors.groupingBy(Equipo::getArea));

        StringBuilder areasHtml = new StringBuilder();
        areasHtml.append("<table class='clean'><thead><tr><th>Área</th><th>Total</th><th>Activos</th><th>Mantenimiento</th></tr></thead><tbody>");
        for (Map.Entry<String, List<Equipo>> entry : porArea.entrySet()) {
            String area = entry.getKey(); List<Equipo> lista = entry.getValue();
            long a = lista.stream().filter(e -> "Activo".equals(e.getEstado())).count();
            long m = lista.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
            areasHtml.append("<tr><td>").append(area).append("</td><td>").append(lista.size())
                    .append("</td><td>").append(a).append("</td><td>").append(m).append("</td></tr>");
        }
        areasHtml.append("</tbody></table>");

        StringBuilder barrasHtml = new StringBuilder();
        long total = activos + mantenimiento + baja + stock;
        if (total > 0) {
            int pA = (int)(activos * 100 / total), pM = (int)(mantenimiento * 100 / total),
                    pB = (int)(baja * 100 / total),    pS = (int)(stock * 100 / total);
            barrasHtml.append("<div style='display:flex;flex-direction:column;gap:.8rem;'>");
            barrasHtml.append(barra("Activos",       pA, "var(--green)"));
            barrasHtml.append(barra("Mantenimiento", pM, "var(--orange)"));
            barrasHtml.append(barra("Baja",          pB, "var(--red)"));
            barrasHtml.append(barra("Stock",         pS, "var(--cyan)"));
            barrasHtml.append("</div>");
        } else {
            barrasHtml.append("<p style='color:var(--muted);'>Sin datos de distribución.</p>");
        }

        List<Equipo> ultimos = equipos.stream()
                .filter(e -> e.getId() != null)
                .sorted(Comparator.comparing(Equipo::getId).reversed())
                .limit(10).collect(Collectors.toList());

        StringBuilder ultimosHtml = new StringBuilder();
        ultimosHtml.append("<div style='display:flex;flex-wrap:wrap;gap:.5rem;'>");
        for (Equipo eq : ultimos) {
            String color = "var(--green)";
            if ("En mantenimiento".equals(eq.getEstado())) color = "var(--orange)";
            if ("Dado de baja".equals(eq.getEstado()))     color = "var(--red)";
            if ("En stock".equals(eq.getEstado()))         color = "var(--cyan)";
            String resp = eq.getResponsable() != null ? eq.getResponsable() : "Desconocido";
            ultimosHtml.append("<a href='/equipo/").append(eq.getCodigo())
                    .append("' style='display:flex;align-items:center;gap:.5rem;padding:.4rem .8rem;background:var(--bg);border:1px solid var(--border);border-radius:8px;text-decoration:none;color:inherit;transition:.2s;' onmouseover=\"this.style.background='#E2E8F0'\" onmouseout=\"this.style.background='var(--bg)'\">");
            ultimosHtml.append("<div style='width:8px;height:8px;border-radius:50%;background:").append(color).append(";'></div>");
            ultimosHtml.append("<span style='font-weight:600;'>").append(eq.getCodigo()).append("</span>");
            ultimosHtml.append("<span style='color:var(--muted);'>— ").append(resp).append("</span>");
            ultimosHtml.append("</a>");
        }
        ultimosHtml.append("</div>");

        agregarDatosUsuario(model);
        model.addAttribute("totalEquipos",    totalEquipos);
        model.addAttribute("activos",         activos);
        model.addAttribute("enMantenimiento", mantenimiento);
        model.addAttribute("dadosDeBaja",     baja);
        model.addAttribute("stock",           stock);
        model.addAttribute("incidentes",      incidentes);
        model.addAttribute("areasActivas",    areasActivas);
        model.addAttribute("disponibilidad",  disponibilidad);
        model.addAttribute("rendimientoTexto",rendimientoTexto);
        model.addAttribute("areasHtml",       areasHtml.toString());
        model.addAttribute("barrasHtml",      barrasHtml.toString());
        model.addAttribute("ultimosHtml",     ultimosHtml.toString());
        return "dashboard";
    }

    private String barra(String label, int pct, String color) {
        return "<div style='display:flex;align-items:center;gap:.8rem;'>" +
                "<span style='width:80px;font-size:.85rem;'>" + label + "</span>" +
                "<div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'>" +
                "<div style='width:" + pct + "%;height:100%;background:" + color + ";border-radius:9px;'></div></div>" +
                "<span style='font-size:.85rem;font-weight:600;'>" + pct + "%</span></div>";
    }

    // ── Lista de equipos ──────────────────────────────────────────────────────

    @GetMapping("/equipos")
    public String listar(Model model) {
        List<Equipo> equipos = equipoService.listarTodos();
        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table class='clean'><thead><tr><th>Código</th><th>Responsable</th><th>Área</th><th>Tipo</th><th>Marca/Modelo</th><th>Serial</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>");
        for (Equipo eq : equipos) {
            String codigo      = eq.getCodigo()          != null ? eq.getCodigo()          : "";
            String responsable = eq.getResponsable()     != null ? eq.getResponsable()     : "";
            String area        = eq.getArea()            != null ? eq.getArea()            : "";
            String tipo        = eq.getTipoDispositivo() != null ? eq.getTipoDispositivo() : "";
            String marcaModelo = (eq.getMarca() != null ? eq.getMarca() : "") + " " + (eq.getModelo() != null ? eq.getModelo() : "");
            String serial      = eq.getSerial()          != null ? eq.getSerial()          : "";
            String estado      = eq.getEstado()          != null ? eq.getEstado()          : "";
            tablaHtml.append("<tr data-codigo='").append(codigo).append("' data-responsable='").append(responsable)
                    .append("' data-area='").append(area).append("' data-serial='").append(serial)
                    .append("' data-estado='").append(estado).append("'>");
            tablaHtml.append("<td>").append(codigo).append("</td><td>").append(responsable)
                    .append("</td><td>").append(area).append("</td><td>").append(tipo)
                    .append("</td><td>").append(marcaModelo).append("</td><td>").append(serial)
                    .append("</td><td>").append(estado)
                    .append("</td><td><a href='/equipo/").append(codigo)
                    .append("' style='color:var(--blue);text-decoration:none;font-weight:500'>Ver ficha</a></td></tr>");
        }
        tablaHtml.append("</tbody></table>");
        agregarDatosUsuario(model);
        model.addAttribute("tablaEquipos", tablaHtml.toString());
        return "equipos/lista";
    }

    // ── Formulario nuevo equipo ───────────────────────────────────────────────

    @GetMapping("/equipos/nuevo")
    public String formularioNuevo(Model model) {
        agregarDatosUsuario(model);
        model.addAttribute("equipoDTO", new EquipoRequestDTO());
        agregarCatalogos(model);
        return "equipos/formulario";
    }

    @PostMapping("/equipos/nuevo")
    public String guardar(@Valid @ModelAttribute("equipoDTO") EquipoRequestDTO dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            agregarDatosUsuario(model);
            agregarCatalogos(model);
            return "equipos/formulario";
        }
        try {
            Equipo creado = equipoService.crear(dto);
            redirectAttrs.addFlashAttribute("mensaje", "Equipo " + creado.getCodigo() + " registrado correctamente");
            return "redirect:/equipo/" + creado.getCodigo();
        } catch (RuntimeException e) {
            result.rejectValue("area", "error.equipo", e.getMessage());
            agregarDatosUsuario(model);
            agregarCatalogos(model);
            return "equipos/formulario";
        }
    }

    // ── Ficha del equipo ──────────────────────────────────────────────────────

    @GetMapping("/equipo/{codigo}")
    public String ficha(@PathVariable String codigo, Model model) throws WriterException, IOException {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        if (equipo == null) return "redirect:/equipos";
        String qrBase64  = equipoService.obtenerQRBase64(codigo);
        agregarDatosUsuario(model);
        agregarCatalogos(model);
        model.addAttribute("equipo",                equipo);
        model.addAttribute("qrBase64",              qrBase64);
        model.addAttribute("historialDTO",          new HistorialEventoDTO());
        model.addAttribute("cambioPropietarioDTO",  new CambioPropietarioDTO());
        model.addAttribute("actualizarHardwareDTO", new ActualizarHardwareDTO());
        return "equipos/ficha";
    }

    // ── Inventario TI ─────────────────────────────────────────────────────────

    @GetMapping("/inventario")
    public String inventarioTi(Model model) {
        agregarDatosUsuario(model);
        model.addAttribute("inventarioEmbedUrl", graphService.obtenerInventarioEmbedUrl());
        return "inventario";
    }

    @GetMapping("/inventario/download")
    public String descargarInventario() {
        return "redirect:" + inventarioDownloadUrl;
    }

    // ── Formatos ──────────────────────────────────────────────────────────────

    @GetMapping("/formatos")
    public String formatos(Model model) {
        agregarDatosUsuario(model);
        List<Map<String, String>> formatosList = new ArrayList<>();
        formatosList.add(Map.of("nombre", "Acta de entrega de activos tecnológicos",  "tipo", "acta-entrega",          "icono", "📄"));
        formatosList.add(Map.of("nombre", "Informe de mantenimiento correctivo",       "tipo", "informe-mantenimiento", "icono", "🔧"));
        formatosList.add(Map.of("nombre", "Acta de baja de activos tecnológicos",      "tipo", "acta-baja",             "icono", "⚠️"));
        model.addAttribute("formatos", formatosList);
        return "formatos";
    }

    @GetMapping("/formatos/ver/{tipo}")
    public String verFormato(@PathVariable String tipo, Model model) {
        agregarDatosUsuario(model);
        String embedUrl = "", nombreFormato = "", downloadUrl = "";
        switch (tipo) {
            case "acta-entrega":
                embedUrl = graphService.obtenerEmbedUrlFormato(actaEntregaUrl);
                nombreFormato = "Acta de entrega de activos tecnológicos";
                downloadUrl   = actaEntregaDownload; break;
            case "informe-mantenimiento":
                embedUrl = graphService.obtenerEmbedUrlFormato(informeMantenimientoUrl);
                nombreFormato = "Informe de mantenimiento correctivo";
                downloadUrl   = informeMantenimientoDownload; break;
            case "acta-baja":
                embedUrl = graphService.obtenerEmbedUrlFormato(actaBajaUrl);
                nombreFormato = "Acta de baja de activos tecnológicos";
                downloadUrl   = actaBajaDownload; break;
            default: return "redirect:/formatos";
        }
        model.addAttribute("embedUrl",      embedUrl);
        model.addAttribute("nombreFormato", nombreFormato);
        model.addAttribute("downloadUrl",   downloadUrl);
        model.addAttribute("tipo",          tipo);
        return "formato-ver";
    }

    @GetMapping("/formatos/descargar/{tipo}")
    public String descargarFormato(@PathVariable String tipo) {
        String url;
        switch (tipo) {
            case "acta-entrega":          url = actaEntregaDownload;          break;
            case "informe-mantenimiento": url = informeMantenimientoDownload; break;
            case "acta-baja":             url = actaBajaDownload;             break;
            default: return "redirect:/formatos?error=formato_no_encontrado";
        }
        if (url == null || url.isBlank()) return "redirect:/formatos?error=url_no_configurada";
        return "redirect:" + url;
    }

    // ── Acciones sobre equipos ────────────────────────────────────────────────

    @PostMapping("/equipos/{codigo}/propietario")
    public String cambiarPropietario(@PathVariable String codigo,
                                     @ModelAttribute CambioPropietarioDTO dto,
                                     RedirectAttributes redirectAttrs) {
        equipoService.cambiarPropietario(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Propietario actualizado correctamente");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/hardware")
    public String actualizarHardware(@PathVariable String codigo,
                                     @ModelAttribute ActualizarHardwareDTO dto,
                                     RedirectAttributes redirectAttrs) {
        equipoService.actualizarHardware(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Hardware actualizado correctamente");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/historial")
    public String agregarHistorial(@PathVariable String codigo,
                                   @Valid @ModelAttribute HistorialEventoDTO dto,
                                   RedirectAttributes redirectAttrs) {
        equipoService.agregarHistorial(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Evento agregado al historial");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/baja")
    public String darDeBaja(@PathVariable String codigo,
                            @RequestParam(required = false) String motivo,
                            RedirectAttributes redirectAttrs) {
        equipoService.darDeBaja(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo dado de baja correctamente");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/mantenimiento")
    public String ponerEnMantenimiento(@PathVariable String codigo,
                                       @RequestParam(required = false) String motivo,
                                       RedirectAttributes redirectAttrs) {
        equipoService.ponerEnMantenimiento(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo enviado a mantenimiento");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/activar")
    public String activar(@PathVariable String codigo, RedirectAttributes redirectAttrs) {
        equipoService.activarEquipo(codigo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo marcado como activo");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/stock")
    public String ponerEnStock(@PathVariable String codigo,
                               @RequestParam(required = false) String motivo,
                               RedirectAttributes redirectAttrs) {
        equipoService.ponerEnStock(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo puesto en stock");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/periferico/{index}")
    public String eliminarPeriferico(@PathVariable String codigo,
                                     @PathVariable int index,
                                     @RequestParam String motivo,
                                     RedirectAttributes redirectAttrs) {
        equipoService.eliminarPeriferico(codigo, index, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Periférico eliminado correctamente");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/periferico/agregar")
    public String agregarPeriferico(@PathVariable String codigo,
                                    @RequestParam String tipo,
                                    @RequestParam(required = false) String marca,
                                    @RequestParam(required = false) String modelo,
                                    @RequestParam(required = false) String serial,
                                    RedirectAttributes redirectAttrs) {
        equipoService.agregarPeriferico(codigo, tipo, marca, modelo, serial);
        redirectAttrs.addFlashAttribute("mensaje", "Periférico agregado correctamente");
        return "redirect:/equipo/" + codigo;
    }
}