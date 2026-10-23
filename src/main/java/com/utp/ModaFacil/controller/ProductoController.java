package com.utp.ModaFacil.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductoController {

    @GetMapping("/productos")
    public String mostrarProductos(Model model, HttpSession session) {

        // Verificar que el usuario inició sesión}
        System.out.println("Session logged: " + session.getAttribute("logged"));
        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }
        System.out.println("Usuario logueado: " + session.getAttribute("usuario"));
        // Categorías de ejemplo
        Categoria polos = new Categoria(1L, "Polos", "#C65F64");
        Categoria pantalones = new Categoria(2L, "Pantalones", "#C65F64");
        Categoria vestidos = new Categoria(3L, "Vestidos", "#C65F64");

        List<Categoria> categorias = List.of(
            polos, pantalones, vestidos
        );

        // Productos de ejemplo
        List<Producto> productos = new ArrayList<>();

        productos.add(new Producto(
            1L, polos, "Polo Básico Negro para Hombre",
            "p_camisero_1.jpg"
        ));

        productos.add(new Producto(
            2L, pantalones, "Pantalón Slim Fit Negro",
            "p_camisero_1.jpg"
        ));

        productos.add(new Producto(
            3L, vestidos, "Vestido Casual Verano",
            "p_camisero_1.jpg"
        ));

        // Enviar información al HTML
        model.addAttribute("categorias", categorias);
        model.addAttribute("productos", productos);
        model.addAttribute("listaRelaciones", List.of());

        return "productos";
    }
}