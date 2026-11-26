package com.utp.ModaFacil.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.dao.InventarioDAO;
import com.utp.ModaFacil.model.ProductoDetalleAdmin;
import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.service.ProductoAdminService;
import java.util.Optional;//

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductoAdminService productoAdminService;
    private final InventarioDAO inventarioDAO;

    public AdminController(ProductoAdminService productoAdminService, InventarioDAO inventarioDAO) {
        this.productoAdminService = productoAdminService;
        this.inventarioDAO = inventarioDAO;
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

        @GetMapping("/productos/{id}/detalle")
        @ResponseBody
        public ResponseEntity<ProductoDetalleAdmin> obtenerDetalleProducto(
                @PathVariable("id") Long idProducto,
                HttpSession session) {

            if (!esAdmin(session)) {
                return ResponseEntity.status(403).build();
            }

            return productoAdminService.obtenerDetalle(idProducto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() ->
                        ResponseEntity.notFound().build()
                    );
        }


    @GetMapping("/inventario")
    public String inventario(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<ProductoInventario> productosInventario = inventarioDAO.listarInventario();
        List<String> categorias = inventarioDAO.listarCategorias();

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

/*Modifique Thirsa*/
    @PostMapping("/inventario/actualizar")
    public String actualizarStock(
            @RequestParam("productoId") Long productoId,
            @RequestParam("proveedores") List<Long> proveedores,
            @RequestParam("tallas") List<String> tallas,
            @RequestParam("cantidades") List<Integer> cantidades,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        inventarioDAO.actualizarStockPorTallas(productoId, tallas, cantidades);
        redirectAttributes.addFlashAttribute("mensajeExito", "Stock actualizado correctamente.");
        return "redirect:/admin/inventario";
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("logged");
        return usuario != null && "ADMIN".equalsIgnoreCase(String.valueOf(usuario.getRol()));
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