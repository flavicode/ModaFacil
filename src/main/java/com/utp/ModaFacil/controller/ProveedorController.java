/*
package com.utp.ModaFacil.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.model.Proveedor;
import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.service.ProveedorService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "busqueda", defaultValue = "") String busqueda,
            @RequestParam(name = "estado", defaultValue = "todos") String estado,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<Proveedor> proveedores = proveedorService.listarProveedores(busqueda, estado);
        model.addAttribute("paginaActual", "proveedores");
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("totalProveedores", proveedores.size());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        return "admin/proveedores";
    }

    @GetMapping("/{id}/detalle")
    @ResponseBody
    public ResponseEntity<Proveedor> detalle(@PathVariable("id") Long id, HttpSession session) {
        if (!esAdmin(session)) {
            return ResponseEntity.status(403).build();
        }
        Optional<Proveedor> proveedor = proveedorService.obtenerPorId(id);
        return proveedor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/registrar")
    public String registrar(
            @ModelAttribute Proveedor proveedor,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        try {
            proveedorService.registrarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proveedor registrado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Ya existe un proveedor con ese RUC o datos inválidos.");
        }
        return "redirect:/admin/proveedores";
    }
/* 
    @PostMapping("/{id}/actualizar")
    @ResponseBody
    public ResponseEntity> actualizar(
            @PathVariable("id") Long id,
            @ModelAttribute Proveedor proveedor,
            HttpSession session) {

        if (!esAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "mensaje", "No tienes permisos."));
        }
        try {
            boolean ok = proveedorService.actualizarProveedor(id, proveedor);
            if (!ok) {
                return ResponseEntity.status(404).body(Map.of("ok", false, "mensaje", "Proveedor no encontrado."));
            }
            return ResponseEntity.ok(Map.of("ok", true, "mensaje", "Proveedor actualizado correctamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", "El RUC ingresado ya está registrado."));
        }
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity> eliminar(@PathVariable("id") Long id, HttpSession session) {
        if (!esAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "mensaje", "No tienes permisos."));
        }
        try {
            boolean ok = proveedorService.eliminarProveedor(id);
            if (!ok) {
                return ResponseEntity.status(404).body(Map.of("ok", false, "mensaje", "Proveedor no encontrado."));
            }
            return ResponseEntity.ok(Map.of("ok", true, "mensaje", "Proveedor eliminado correctamente."));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "mensaje", "No se puede eliminar porque tiene ofertas o productos asociados.")
            );
        }
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("logged");
        return usuario != null && "ADMIN".equalsIgnoreCase(String.valueOf(usuario.getRol()));
    }
}
*/