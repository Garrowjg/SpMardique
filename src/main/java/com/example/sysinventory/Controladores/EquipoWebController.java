package com.example.sysinventory.Controladores;

import com.example.sysinventory.DTOs.*;
import com.example.sysinventory.Modelos.Equipo;
import com.example.sysinventory.Servicios.EquipoService;
import com.example.sysinventory.Servicios.GraphService;
import com.example.sysinventory.Servicios.TokenService;
import com.example.sysinventory.Utilidades.CodigoGenerador;
import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

    private final EquipoService equipoService;
    private final GraphService graphService;
    private final TokenService tokenService;

    public EquipoWebController(EquipoService equipoService,
                               GraphService graphService,
                               TokenService tokenService) {
        this.equipoService = equipoService;
        this.graphService = graphService;
        this.tokenService = tokenService;
    }

    // ================= UTILIDAD =================
    private void agregarDatosUsuario(Model model) {
        String userName = "Usuario";
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
                if (nombre == null || nombre.isBlank()) {
                    nombre = oauth2User.getAttribute("preferred_username");
                }
                userName = (nombre != null && !nombre.isBlank()) ? nombre : "Usuario";
                userEmail = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email") : "";
            } else {
                userName = auth.getName();
            }
        }
        model.addAttribute("userName", userName);
        model.addAttribute("userEmail", userEmail);
    }

    // ================= REDIRECCIONES =================
    @GetMapping("/redirect/{codigo}")
    public String redirigirAExcel(@PathVariable String codigo) {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        if (equipo == null) return "redirect:/equipos";
        return "redirect:/equipo/" + codigo + "/hoja";
    }

    @GetMapping("/equipo/{codigo}/hoja")
    public String hojaVida(@PathVariable String codigo,
                           HttpSession session,
                           Model model) {
        agregarDatosUsuario(model);

        String token = tokenService.getAccessToken();
        if (token == null) {
            token = (String) session.getAttribute("accessToken");
            if (token == null) {
                return "redirect:/auth/login";
            }
            tokenService.setAccessToken(token);
        }

        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        if (equipo == null) {
            return "redirect:/equipos";
        }

        String serial = equipo.getSerial() != null ? equipo.getSerial().trim() : "";
        String embedUrl = "";
        if (!serial.isBlank()) {
            embedUrl = graphService.obtenerEmbedUrl(serial, token);
        }

        model.addAttribute("equipo", equipo);
        model.addAttribute("excelEmbedUrl", embedUrl);
        return "equipos/hoja";
    }

    // ================= DASHBOARD =================
    @GetMapping("/")
    public String dashboard(Model model) {
        List<Equipo> equipos = equipoService.listarTodos();

        long activos       = equipos.stream().filter(e -> "Activo".equals(e.getEstado())).count();
        long mantenimiento = equipos.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
        long baja          = equipos.stream().filter(e -> "Dado de baja".equals(e.getEstado())).count();
        long stock         = equipos.stream().filter(e -> "En stock".equals(e.getEstado())).count();
        long totalEquipos  = equipos.size();

        long incidentes = mantenimiento + baja;
        long areasActivas = equipos.stream()
                .filter(e -> "Activo".equals(e.getEstado()) && e.getArea() != null && !e.getArea().isBlank())
                .map(Equipo::getArea)
                .distinct()
                .count();
        int disponibilidad = totalEquipos > 0 ? (int)(activos * 100 / totalEquipos) : 0;
        String rendimientoTexto = disponibilidad >= 90 ? "Óptimo" :
                disponibilidad >= 70 ? "Bueno" :
                disponibilidad >= 50 ? "Regular" : "Crítico";

        Map<String, List<Equipo>> porArea = equipos.stream()
                .filter(e -> e.getArea() != null && !e.getArea().isBlank())
                .collect(Collectors.groupingBy(Equipo::getArea));

        StringBuilder areasHtml = new StringBuilder();
        areasHtml.append("<table class='clean'>");
        areasHtml.append("<thead><tr><th>Área</th><th>Total</th><th>Activos</th><th>Mantenimiento</th></tr></thead>");
        areasHtml.append("<tbody>");
        for (Map.Entry<String, List<Equipo>> entry : porArea.entrySet()) {
            String area = entry.getKey();
            List<Equipo> lista = entry.getValue();
            long a = lista.stream().filter(e -> "Activo".equals(e.getEstado())).count();
            long m = lista.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
            areasHtml.append("<tr>")
                    .append("<td>").append(area).append("</td>")
                    .append("<td>").append(lista.size()).append("</td>")
                    .append("<td>").append(a).append("</td>")
                    .append("<td>").append(m).append("</td>")
                    .append("</tr>");
        }
        areasHtml.append("</tbody>");
        areasHtml.append("</table>");

        StringBuilder barrasHtml = new StringBuilder();
        long total = activos + mantenimiento + baja + stock;
        if (total > 0) {
            int pctActivos = (int)(activos * 100 / total);
            int pctMantenimiento = (int)(mantenimiento * 100 / total);
            int pctBaja = (int)(baja * 100 / total);
            int pctStock = (int)(stock * 100 / total);
            barrasHtml.append("<div style='display:flex;flex-direction:column;gap:.8rem;'>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Activos</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctActivos).append("%;height:100%;background:var(--green);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctActivos).append("%</span></div>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Mantenimiento</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctMantenimiento).append("%;height:100%;background:var(--orange);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctMantenimiento).append("%</span></div>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Baja</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctBaja).append("%;height:100%;background:var(--red);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctBaja).append("%</span></div>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Stock</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctStock).append("%;height:100%;background:var(--cyan);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctStock).append("%</span></div>");
            barrasHtml.append("</div>");
        } else {
            barrasHtml.append("<p style='color:var(--muted);'>Sin datos de distribución.</p>");
        }

        List<Equipo> ultimos = equipos.stream()
                .filter(e -> e.getId() != null)
                .sorted(Comparator.comparing(Equipo::getId).reversed())
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder ultimosHtml = new StringBuilder();
        ultimosHtml.append("<div style='display:flex;flex-wrap:wrap;gap:.5rem;'>");
        for (Equipo eq : ultimos) {
            String color = "var(--green)";
            if("En mantenimiento".equals(eq.getEstado())) color = "var(--orange)";
            if("Dado de baja".equals(eq.getEstado())) color = "var(--red)";
            if("En stock".equals(eq.getEstado())) color = "var(--cyan)";
            String responsableMostrar = eq.getResponsable() != null ? eq.getResponsable() : "Desconocido";
            ultimosHtml.append("<div style='display:flex;align-items:center;gap:.5rem;padding:.4rem .8rem;background:var(--bg);border:1px solid var(--border);border-radius:8px;'>");
            ultimosHtml.append("<div style='width:8px;height:8px;border-radius:50%;background:").append(color).append(";'></div>");
            ultimosHtml.append("<span style='font-weight:600;'>").append(eq.getCodigo()).append("</span>");
            ultimosHtml.append("<span style='color:var(--muted);'>— ").append(responsableMostrar).append("</span>");
            ultimosHtml.append("</div>");
        }
        ultimosHtml.append("</div>");

        agregarDatosUsuario(model);
        model.addAttribute("totalEquipos", totalEquipos);
        model.addAttribute("activos", activos);
        model.addAttribute("enMantenimiento", mantenimiento);
        model.addAttribute("dadosDeBaja", baja);
        model.addAttribute("stock", stock);
        model.addAttribute("incidentes", incidentes);
        model.addAttribute("areasActivas", areasActivas);
        model.addAttribute("disponibilidad", disponibilidad);
        model.addAttribute("rendimientoTexto", rendimientoTexto);
        model.addAttribute("areasHtml", areasHtml.toString());
        model.addAttribute("barrasHtml", barrasHtml.toString());
        model.addAttribute("ultimosHtml", ultimosHtml.toString());

        return "dashboard";
    }

    // ================= LISTA DE EQUIPOS =================
    @GetMapping("/equipos")
    public String listar(Model model) {
        List<Equipo> equipos = equipoService.listarTodos();

        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table class='clean'>");
        tablaHtml.append("<thead><tr><th>Código</th><th>Responsable</th><th>Área</th><th>Tipo</th><th>Marca/Modelo</th><th>Serial</th><th>Estado</th><th>Acciones</th></tr></thead>");
        tablaHtml.append("<tbody>");
        for (Equipo eq : equipos) {
            String codigo = eq.getCodigo() != null ? eq.getCodigo() : "";
            String responsable = eq.getResponsable() != null ? eq.getResponsable() : "";
            String area = eq.getArea() != null ? eq.getArea() : "";
            String tipo = eq.getTipoDispositivo() != null ? eq.getTipoDispositivo() : "";
            String marcaModelo = (eq.getMarca() != null ? eq.getMarca() : "") + " " + (eq.getModelo() != null ? eq.getModelo() : "");
            String serial = eq.getSerial() != null ? eq.getSerial() : "";
            String estado = eq.getEstado() != null ? eq.getEstado() : "";

            tablaHtml.append("<tr data-codigo='").append(codigo).append("' data-responsable='").append(responsable).append("' data-area='").append(area).append("' data-serial='").append(serial).append("' data-estado='").append(estado).append("'>");
            tablaHtml.append("<td>").append(codigo).append("</td>");
            tablaHtml.append("<td>").append(responsable).append("</td>");
            tablaHtml.append("<td>").append(area).append("</td>");
            tablaHtml.append("<td>").append(tipo).append("</td>");
            tablaHtml.append("<td>").append(marcaModelo).append("</td>");
            tablaHtml.append("<td>").append(serial).append("</td>");
            tablaHtml.append("<td>").append(estado).append("</td>");
            tablaHtml.append("<td><a href='/equipo/").append(codigo).append("' style='color:var(--blue);text-decoration:none;font-weight:500'>Ver ficha</a></td>");
            tablaHtml.append("</tr>");
        }
        tablaHtml.append("</tbody>");
        tablaHtml.append("</table>");

        agregarDatosUsuario(model);
        model.addAttribute("tablaEquipos", tablaHtml.toString());
        return "equipos/lista";
    }

    // ================= FORMULARIO NUEVO =================
    @GetMapping("/equipos/nuevo")
    public String formularioNuevo(Model model) {
        agregarDatosUsuario(model);
        model.addAttribute("equipoDTO", new EquipoRequestDTO());
        model.addAttribute("areas", CodigoGenerador.getAreas().keySet().stream().sorted().collect(Collectors.toList()));
        model.addAttribute("cargos", CodigoGenerador.getCargos().keySet().stream().sorted().collect(Collectors.toList()));
        model.addAttribute("marcasModelos", CodigoGenerador.getMarcasModelos());
        model.addAttribute("opcionesRam", CodigoGenerador.getOpcionesRam());
        model.addAttribute("opcionesDisco", CodigoGenerador.getOpcionesDisco());
        return "equipos/formulario";
    }

    @PostMapping("/equipos/nuevo")
    public String guardar(@Valid @ModelAttribute("equipoDTO") EquipoRequestDTO dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            agregarDatosUsuario(model);
            model.addAttribute("areas", CodigoGenerador.getAreas().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("cargos", CodigoGenerador.getCargos().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("marcasModelos", CodigoGenerador.getMarcasModelos());
            model.addAttribute("opcionesRam", CodigoGenerador.getOpcionesRam());
            model.addAttribute("opcionesDisco", CodigoGenerador.getOpcionesDisco());
            return "equipos/formulario";
        }
        try {
            Equipo creado = equipoService.crear(dto);
            redirectAttrs.addFlashAttribute("mensaje", "Equipo " + creado.getCodigo() + " registrado correctamente");
            return "redirect:/equipo/" + creado.getCodigo();
        } catch (RuntimeException e) {
            result.rejectValue("area", "error.equipo", e.getMessage());
            agregarDatosUsuario(model);
            model.addAttribute("areas", CodigoGenerador.getAreas().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("cargos", CodigoGenerador.getCargos().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("marcasModelos", CodigoGenerador.getMarcasModelos());
            model.addAttribute("opcionesRam", CodigoGenerador.getOpcionesRam());
            model.addAttribute("opcionesDisco", CodigoGenerador.getOpcionesDisco());
            return "equipos/formulario";
        }
    }

    // ================= FICHA DEL EQUIPO =================
    @GetMapping("/equipo/{codigo}")
    public String ficha(@PathVariable String codigo, Model model) throws WriterException, IOException {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        String qrBase64 = equipoService.obtenerQRBase64(codigo);
        agregarDatosUsuario(model);
        model.addAttribute("equipo", equipo);
        model.addAttribute("qrBase64", qrBase64);
        model.addAttribute("historialDTO", new HistorialEventoDTO());
        model.addAttribute("cambioPropietarioDTO", new CambioPropietarioDTO());
        model.addAttribute("actualizarHardwareDTO", new ActualizarHardwareDTO());
        model.addAttribute("areas", CodigoGenerador.getAreas().keySet().stream().sorted().collect(Collectors.toList()));
        model.addAttribute("cargos", CodigoGenerador.getCargos().keySet().stream().sorted().collect(Collectors.toList()));
        model.addAttribute("opcionesRam", CodigoGenerador.getOpcionesRam());
        model.addAttribute("opcionesDisco", CodigoGenerador.getOpcionesDisco());
        return "equipos/ficha";
    }

    // ================= ACCIONES SOBRE EQUIPOS =================
    @PostMapping("/equipos/{codigo}/propietario")
    public String cambiarPropietario(@PathVariable String codigo,
                                     @ModelAttribute("cambioPropietarioDTO") CambioPropietarioDTO dto,
                                     RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.cambiarPropietario(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Propietario actualizado correctamente");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/hardware")
    public String actualizarHardware(@PathVariable String codigo,
                                     @ModelAttribute("actualizarHardwareDTO") ActualizarHardwareDTO dto,
                                     RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.actualizarHardware(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Hardware actualizado correctamente");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/historial")
    public String agregarHistorial(@PathVariable String codigo,
                                   @Valid @ModelAttribute("historialDTO") HistorialEventoDTO dto,
                                   RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.agregarHistorial(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Evento agregado al historial");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/baja")
    public String darDeBaja(@PathVariable String codigo,
                            @RequestParam(required = false) String motivo,
                            RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.darDeBaja(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo dado de baja correctamente");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/mantenimiento")
    public String ponerEnMantenimiento(@PathVariable String codigo,
                                       @RequestParam(required = false) String motivo,
                                       RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.ponerEnMantenimiento(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo enviado a mantenimiento");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/activar")
    public String activar(@PathVariable String codigo, RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.activarEquipo(codigo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo marcado como activo");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
    }

    @PostMapping("/equipos/{codigo}/stock")
    public String ponerEnStock(@PathVariable String codigo,
                               @RequestParam(required = false) String motivo,
                               RedirectAttributes redirectAttrs) {
        Equipo equipoActualizado = equipoService.ponerEnStock(codigo, motivo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo puesto en stock");
        return "redirect:/equipo/" + equipoActualizado.getCodigo();
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

    // ================= NUEVO: AGREGAR PERIFÉRICO =================
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