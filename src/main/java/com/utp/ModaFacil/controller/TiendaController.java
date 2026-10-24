
package com.utp.ModaFacil.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class TiendaController implements ErrorController {

    // Vistas publicas
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("paginaActual", "inicio");
        return "inicio";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("paginaActual", "contacto");
        return "contacto";
    }


    // Vistas exclusivas de usuarios autenticados

    @GetMapping("/comparar")
    public String comparar(
            @RequestParam(required = false) Long id,
            Model model,
            HttpSession session) {
        model.addAttribute("paginaActual", "comparar");
        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }
        if (id == null) {
            model.addAttribute("productoSeleccionado", null);
            return "comparar";
        }
        return "comparar";

    }

    // Cerrar sesion
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Pagina de error
    @RequestMapping("/error")
    public String manejarError() {
        return "error404";
    }

    // Visualizar layout
    @GetMapping("/layout")
    public String layout() {
        return "layout";
    }
}

