package com.utp.ModaFacil.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.service.ProductoAdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductoAdminService productoAdminService;

    public AdminController(ProductoAdminService productoAdminService) {
        this.productoAdminService = productoAdminService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("paginaActual", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/productos")
    public String productos(
            @RequestParam(name = "busqueda", defaultValue = "")
            String busqueda,

            @RequestParam(name = "estado", defaultValue = "todos")
            String estado,

            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("paginaActual", "productos");
        model.addAttribute("productos",productoAdminService.buscarProductos(busqueda, estado));
        model.addAttribute("totalProductos",productoAdminService.contarProductos());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        return "admin/productos";
    }
    @PostMapping("/productos/{id}/estado")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> actualizarEstadoProducto(
                @PathVariable("id") Long idProducto,
                @RequestParam("activo") boolean activo,
                HttpSession session) {
            if (!esAdmin(session)) {
                return ResponseEntity.status(403).body(Map.of("ok", false,"mensaje", "No tienes permisos de administrador"));
            }
            boolean actualizado =productoAdminService.actualizarEstado(idProducto, activo);
            if (!actualizado) {
                return ResponseEntity.status(404).body(Map.of("ok", false,"mensaje", "No se pudo actualizar el producto"));
            }
            return ResponseEntity.ok(
                Map.of("ok", true,"activo", activo,"mensaje", "Estado actualizado correctamente"));
        }

    @GetMapping("/inventario")
    public String inventario(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

    // TODO: Reemplazar con datos reales desde tu ProductoService / InventarioRepository
        List<ProductoInventario> productosInventario = obtenerDatosDemoInventario();

        int totalUnidades = productosInventario.stream()
                .mapToInt(ProductoInventario::getStockTotal)
                .sum();

        long productosConStock = productosInventario.stream()
                .filter(p -> p.getStockTotal() > 0)
                .count();

        long productosAgotados = productosInventario.stream()
                .filter(p -> p.getStockTotal() == 0)
                .count();

        model.addAttribute("paginaActual", "inventario");
        model.addAttribute("productos", productosInventario);
        model.addAttribute("totalUnidades", totalUnidades);
        model.addAttribute("productosConStock", productosConStock);
        model.addAttribute("productosAgotados", productosAgotados);
        model.addAttribute("totalRegistros", productosInventario.size());

        return "admin/inventario";
    }

    @PostMapping("/inventario/actualizar")
    public String actualizarStock(
            @RequestParam("productoId") Long productoId,
            @RequestParam("tallas") List<String> tallas,
            @RequestParam("cantidades") List<Integer> cantidades,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // TODO: Guardar en base de datos usando tu servicio:
        // inventarioService.actualizarStockPorTallas(productoId, tallas, cantidades);
        redirectAttributes.addFlashAttribute("mensajeExito", "Stock actualizado correctamente.");
        return "redirect:/admin/inventario";
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("logged");
        return usuario != null && "ADMIN".equalsIgnoreCase(String.valueOf(usuario.getRol()));
    }

    // Datos de prueba idénticos al mockup (puedes borrar este método al conectar tu BD)
    private List<ProductoInventario> obtenerDatosDemoInventario() {
        List<ProductoInventario> lista = new ArrayList<>();

        lista.add(new ProductoInventario(1L, "P00125", "Blusa Elegante", "Blusas", "/img/productos/blusa.png", 25,
                List.of(
                    new TallaStock("S", 8),
                    new TallaStock("M", 12),
                    new TallaStock("L", 5),
                    new TallaStock("XL", 0)
                )));

        lista.add(new ProductoInventario(2L, "P00124", "Jean Clásico", "Jeans", "/img/productos/jean.png", 18,
                List.of(
                    new TallaStock("28", 6),
                    new TallaStock("30", 8),
                    new TallaStock("32", 4),
                    new TallaStock("34", 0)
                )));

        lista.add(new ProductoInventario(3L, "P00123", "Casaca Urbana", "Casacas", "/img/productos/casaca.png", 12,
                List.of(
                    new TallaStock("S", 4),
                    new TallaStock("M", 5),
                    new TallaStock("L", 3),
                    new TallaStock("XL", 0)
                )));

        lista.add(new ProductoInventario(4L, "P00122", "Vestido Floral", "Vestidos", "/img/productos/vestido.png", 0,
                List.of(
                    new TallaStock("S", 0),
                    new TallaStock("M", 0),
                    new TallaStock("L", 0)
                )));

        lista.add(new ProductoInventario(5L, "P00121", "Polo Básico", "Polos", "/img/productos/polo.png", 40,
                List.of(
                    new TallaStock("S", 10),
                    new TallaStock("M", 15),
                    new TallaStock("L", 10),
                    new TallaStock("XL", 5)
                )));

        return lista;
    }

    public static class ProductoInventario {
        private final Long id;
        private final String codigo;
        private final String nombre;
        private final String categoria;
        private final String imagen;
        private final int stockTotal;
        private final List<TallaStock> tallas;

        public ProductoInventario(Long id, String codigo, String nombre, String categoria, String imagen, int stockTotal, List<TallaStock> tallas) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.categoria = categoria;
            this.imagen = imagen;
            this.stockTotal = stockTotal;
            this.tallas = tallas;
        }

        public Long getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getImagen() { return imagen; }
        public int getStockTotal() { return stockTotal; }
        public List<TallaStock> getTallas() { return tallas; }
    }

    public static class TallaStock {
        private final String talla;
        private final int stock;

        public TallaStock(String talla, int stock) {
            this.talla = talla;
            this.stock = stock;
        }

        public String getTalla() { return talla; }
        public int getStock() { return stock; }
    }
}