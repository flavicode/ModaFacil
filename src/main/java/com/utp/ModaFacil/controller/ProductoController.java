package com.utp.ModaFacil.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        model.addAttribute("paginaActual", "productos");
        // Verificar sesión
        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        // CATEGORÍAS DE EJEMPLO
        Categoria polos = new Categoria(1L, "Polos", "#C65F64");
        Categoria pantalones = new Categoria(2L, "Pantalones", "#C65F64");
        Categoria vestidos = new Categoria(3L, "Vestidos", "#C65F64");

        List<Categoria> categorias = List.of(
            polos, pantalones, vestidos
        );

        // PRODUCTOS DE EJEMPLO
        Producto polo = new Producto(
            1L, polos,
            "Polo Básico Negro para Hombre",
            "p_camisero_1.jpg"
        );

        Producto pantalon = new Producto(
            2L, pantalones,
            "Pantalón Slim Fit Negro",
            "p_camisero_1.jpg"
        );

        Producto vestido = new Producto(
            3L, vestidos,
            "Vestido Casual Verano",
            "p_camisero_1.jpg"
        );

        List<Producto> productos = List.of(
            polo, pantalon, vestido
        );

        // RELACIONES SIMULADAS DE PRODUCTO Y PROVEEDOR
        List<Map<String, Object>> relaciones = new ArrayList<>();

        // POLO: 3 PROVEEDORES
        agregarRelacion(relaciones, polo, "Hombre", 1L, 39.90);
        agregarRelacion(relaciones, polo, "Hombre", 2L, 45.00);
        agregarRelacion(relaciones, polo, "Hombre", 3L, 42.00);

        // PANTALÓN: 8 PROVEEDORES
        agregarRelacion(relaciones, pantalon, "Hombre", 1L, 25.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 2L, 28.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 3L, 32.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 4L, 27.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 5L, 30.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 6L, 29.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 7L, 35.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 8L, 31.00);

        // VESTIDO: 6 PROVEEDORES
        agregarRelacion(relaciones, vestido, "Mujer", 1L, 42.00);
        agregarRelacion(relaciones, vestido, "Mujer", 2L, 48.00);
        agregarRelacion(relaciones, vestido, "Mujer", 3L, 50.00);
        agregarRelacion(relaciones, vestido, "Mujer", 4L, 46.00);
        agregarRelacion(relaciones, vestido, "Mujer", 5L, 55.00);
        agregarRelacion(relaciones, vestido, "Mujer", 6L, 49.00);

        // ENVIAR DATOS A THYMELEAF
        model.addAttribute("categorias", categorias);
        model.addAttribute("productos", productos);
        model.addAttribute("relacionesServidor", relaciones);

        return "productos";
    }

    // CREAR RELACIÓN SIMULADA
    private void agregarRelacion(
            List<Map<String, Object>> relaciones,
            Producto producto,
            String genero,
            Long idImportador,
            double precio) {

        Map<String, Object> productoData = new HashMap<>();

        productoData.put("idProducto", producto.getIdProducto());
        productoData.put("nombre", producto.getNombre());
        productoData.put("imagen", producto.getImagen());
        productoData.put("genero", genero);

        Map<String, Object> categoriaData = new HashMap<>();
        categoriaData.put(
            "nombre",
            producto.getCategoria().getNombre()
        );

        productoData.put("categoria", categoriaData);

        Map<String, Object> importadorData = new HashMap<>();
        importadorData.put("idImportador", idImportador);

        Map<String, Object> relacion = new HashMap<>();
        relacion.put("producto", productoData);
        relacion.put("importador", importadorData);
        relacion.put("precio", precio);

        relaciones.add(relacion);
    }
}