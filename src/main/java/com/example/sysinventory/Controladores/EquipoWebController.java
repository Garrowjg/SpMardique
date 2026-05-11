package com.example.sysinventory.Controladores;

import com.example.sysinventory.DTOs.*;
import com.example.sysinventory.Modelos.Equipo;
import com.example.sysinventory.Servicios.EquipoService;
import com.example.sysinventory.Utilidades.CodigoGenerador;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EquipoWebController {

    private final EquipoService equipoService;

    public EquipoWebController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    // ── Redirección QR → SharePoint ───────────────────────────────────────
    @GetMapping("/redirect/{codigo}")
    public String redirigirAExcel(@PathVariable String codigo) {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        String serial = equipo.getSerial();
        if (serial == null || serial.isBlank()) {
            return "redirect:/equipo/" + codigo;
        }
        String hoja = URLEncoder.encode(serial, StandardCharsets.UTF_8);
        String sharepointUrl = "https://mardique.sharepoint.com/sites/IT/Documentos/Hojas%20de%20Vida.xlsx"
                + "?web=1&activeCell=" + hoja + "!A1";
        return "redirect:" + sharepointUrl;
    }

    // ── Dashboard ─────────────────────────────────────────────────────────
    @GetMapping("/")
    public String dashboard(Model model) {
        List<Equipo> equipos = equipoService.listarTodos();

        long activos       = equipos.stream().filter(e -> "Activo".equals(e.getEstado())).count();
        long mantenimiento = equipos.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
        long baja          = equipos.stream().filter(e -> "Dado de baja".equals(e.getEstado())).count();

        // Tabla de áreas (HTML directo con clase "clean")
        Map<String, List<Equipo>> porArea = equipos.stream()
                .filter(e -> e.getArea() != null && !e.getArea().isBlank())
                .collect(Collectors.groupingBy(Equipo::getArea));

        StringBuilder areasHtml = new StringBuilder();
        areasHtml.append("<table class='clean'>");
        areasHtml.append("<thead><tr><th>Área</th><th>Total</th><th>Activos</th><th>Mantenimiento</th><th>Baja</th></tr></thead>");
        areasHtml.append("<tbody>");
        for (Map.Entry<String, List<Equipo>> entry : porArea.entrySet()) {
            String area = entry.getKey();
            List<Equipo> lista = entry.getValue();
            long a = lista.stream().filter(e -> "Activo".equals(e.getEstado())).count();
            long m = lista.stream().filter(e -> "En mantenimiento".equals(e.getEstado())).count();
            long b = lista.stream().filter(e -> "Dado de baja".equals(e.getEstado())).count();
            areasHtml.append("<tr>")
                    .append("<td>").append(area).append("</td>")
                    .append("<td>").append(lista.size()).append("</td>")
                    .append("<td>").append(a).append("</td>")
                    .append("<td>").append(m).append("</td>")
                    .append("<td>").append(b).append("</td>")
                    .append("</tr>");
        }
        areasHtml.append("</tbody></table>");

        // Barras de distribución (gráfico CSS)
        StringBuilder barrasHtml = new StringBuilder();
        long total = activos + mantenimiento + baja;
        if (total > 0) {
            int pctActivos = (int)(activos * 100 / total);
            int pctMantenimiento = (int)(mantenimiento * 100 / total);
            int pctBaja = (int)(baja * 100 / total);
            barrasHtml.append("<div style='display:flex;flex-direction:column;gap:.8rem;'>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Activos</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctActivos).append("%;height:100%;background:var(--green);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctActivos).append("%</span></div>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Mantenimiento</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctMantenimiento).append("%;height:100%;background:var(--orange);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctMantenimiento).append("%</span></div>");
            barrasHtml.append("<div style='display:flex;align-items:center;gap:.8rem;'><span style='width:80px;font-size:.85rem;'>Baja</span><div style='flex:1;height:18px;background:#E2E8F0;border-radius:9px;overflow:hidden;'><div style='width:").append(pctBaja).append("%;height:100%;background:var(--red);border-radius:9px;'></div></div><span style='font-size:.85rem;font-weight:600;'>").append(pctBaja).append("%</span></div>");
            barrasHtml.append("</div>");
        } else {
            barrasHtml.append("<p style='color:var(--muted);'>Sin datos de distribución.</p>");
        }

        // Últimos registros (tarjetas simples)
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
            ultimosHtml.append("<div style='display:flex;align-items:center;gap:.5rem;padding:.4rem .8rem;background:var(--bg);border:1px solid var(--border);border-radius:8px;'>");
            ultimosHtml.append("<div style='width:8px;height:8px;border-radius:50%;background:").append(color).append(";'></div>");
            ultimosHtml.append("<span style='font-weight:600;'>").append(eq.getCodigo()).append("</span>");
            ultimosHtml.append("<span style='color:var(--muted);'>— ").append(eq.getResponsable()).append("</span>");
            ultimosHtml.append("</div>");
        }
        ultimosHtml.append("</div>");

        model.addAttribute("totalEquipos", equipos.size());
        model.addAttribute("activos", activos);
        model.addAttribute("enMantenimiento", mantenimiento);
        model.addAttribute("dadosDeBaja", baja);
        model.addAttribute("areasHtml", areasHtml.toString());
        model.addAttribute("barrasHtml", barrasHtml.toString());
        model.addAttribute("ultimosHtml", ultimosHtml.toString());
        return "dashboard";
    }

    // ── Lista equipos (con atributos data- para filtros) ──────────────────
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
        tablaHtml.append("</tbody></table>");

        model.addAttribute("tablaEquipos", tablaHtml.toString());
        return "equipos/lista";
    }

    // ── Formulario nuevo ──────────────────────────────────────────────────
    @GetMapping("/equipos/nuevo")
    public String formularioNuevo(Model model) {
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
            model.addAttribute("areas", CodigoGenerador.getAreas().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("cargos", CodigoGenerador.getCargos().keySet().stream().sorted().collect(Collectors.toList()));
            model.addAttribute("marcasModelos", CodigoGenerador.getMarcasModelos());
            model.addAttribute("opcionesRam", CodigoGenerador.getOpcionesRam());
            model.addAttribute("opcionesDisco", CodigoGenerador.getOpcionesDisco());
            return "equipos/formulario";
        }
        Equipo creado = equipoService.crear(dto);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo " + creado.getCodigo() + " registrado correctamente");
        return "redirect:/equipo/" + creado.getCodigo();
    }

    // ── Ficha equipo ──────────────────────────────────────────────────────
    @GetMapping("/equipo/{codigo}")
    public String ficha(@PathVariable String codigo, Model model) throws WriterException, IOException {
        Equipo equipo = equipoService.buscarPorCodigo(codigo);
        String qrBase64 = equipoService.obtenerQRBase64(codigo);
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

    // ── Cambiar propietario ───────────────────────────────────────────────
    @PostMapping("/equipos/{codigo}/propietario")
    public String cambiarPropietario(@PathVariable String codigo,
                                     @ModelAttribute("cambioPropietarioDTO") CambioPropietarioDTO dto,
                                     RedirectAttributes redirectAttrs) {
        equipoService.cambiarPropietario(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Propietario actualizado correctamente");
        return "redirect:/equipo/" + codigo;
    }

    // ── Actualizar hardware ───────────────────────────────────────────────
    @PostMapping("/equipos/{codigo}/hardware")
    public String actualizarHardware(@PathVariable String codigo,
                                     @ModelAttribute("actualizarHardwareDTO") ActualizarHardwareDTO dto,
                                     RedirectAttributes redirectAttrs) {
        equipoService.actualizarHardware(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Hardware actualizado correctamente");
        return "redirect:/equipo/" + codigo;
    }

    // ── Historial manual ──────────────────────────────────────────────────
    @PostMapping("/equipos/{codigo}/historial")
    public String agregarHistorial(@PathVariable String codigo,
                                   @Valid @ModelAttribute("historialDTO") HistorialEventoDTO dto,
                                   RedirectAttributes redirectAttrs) {
        equipoService.agregarHistorial(codigo, dto);
        redirectAttrs.addFlashAttribute("mensaje", "Evento agregado al historial");
        return "redirect:/equipo/" + codigo;
    }

    // ── Estados ───────────────────────────────────────────────────────────
    @PostMapping("/equipos/{codigo}/baja")
    public String darDeBaja(@PathVariable String codigo, RedirectAttributes redirectAttrs) {
        equipoService.darDeBaja(codigo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo dado de baja correctamente");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/mantenimiento")
    public String ponerEnMantenimiento(@PathVariable String codigo, RedirectAttributes redirectAttrs) {
        equipoService.ponerEnMantenimiento(codigo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo enviado a mantenimiento");
        return "redirect:/equipo/" + codigo;
    }

    @PostMapping("/equipos/{codigo}/activar")
    public String activar(@PathVariable String codigo, RedirectAttributes redirectAttrs) {
        equipoService.activarEquipo(codigo);
        redirectAttrs.addFlashAttribute("mensaje", "Equipo marcado como activo");
        return "redirect:/equipo/" + codigo;
    }
}