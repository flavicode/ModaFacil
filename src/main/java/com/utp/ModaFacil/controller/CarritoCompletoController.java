package com.utp.ModaFacil.controller;

import com.utp.ModaFacil.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarritoCompletoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        model.addAttribute("items", carritoService.obtenerItems(session));
        model.addAttribute("subtotal", carritoService.calcularSubtotal(session));

        return "carrito";
    }
}
