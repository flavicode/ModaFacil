
package com.utp.ModaFacil.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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