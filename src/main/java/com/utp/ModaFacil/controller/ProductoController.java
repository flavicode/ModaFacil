package com.utp.ModaFacil.controller;

import com.utp.ModaFacil.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductoController {

    private final CatalogoService catalogoService;

    public ProductoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/productos")
    public String mostrarProductos(
            Model model,
            HttpSession session) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaActual", "productos");

        model.addAttribute(
            "relacionesServidor",
            catalogoService.obtenerRelaciones()
        );

        return "productos";
    }
}