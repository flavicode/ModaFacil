package com.utp.ModaFacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.utp.ModaFacil.model.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("paginaActual", "dashboard");
        return "admin/dashboard";
    }

    // Método auxiliar para validar sesión y rol ADMIN
    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("logged");
        return usuario != null && "ADMIN".equalsIgnoreCase(String.valueOf(usuario.getRol()));
    }
}
