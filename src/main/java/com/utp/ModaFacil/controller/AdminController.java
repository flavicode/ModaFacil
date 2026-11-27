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
import com.utp.ModaFacil.model.ProductoEditarAdmin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataAccessException;

import com.utp.ModaFacil.dao.InventarioDAO;
import com.utp.ModaFacil.model.ProductoDetalleAdmin;
import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.model.ProductoInventario;
import com.utp.ModaFacil.service.ProductoAdminService;
import java.io.IOException;
import com.utp.ModaFacil.service.ImagenProductoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductoAdminService productoAdminService;
    private final InventarioDAO inventarioDAO;
    private final ImagenProductoService imagenProductoService;

    public AdminController(ProductoAdminService productoAdminService, InventarioDAO inventarioDAO, ImagenProductoService imagenProductoService) {
        this.productoAdminService = productoAdminService;
        this.inventarioDAO = inventarioDAO;
        this.imagenProductoService = imagenProductoService;
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
        model.addAttribute("categoriasPorGenero", productoAdminService.obtenerCategoriasPorGenero());
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

        @PostMapping("/productos/{id}/actualizar")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> actualizarProducto(
                @PathVariable("id") Long idProducto,
                @ModelAttribute ProductoEditarAdmin producto,
                @RequestParam(name = "imagen", required = false) MultipartFile imagen,
                HttpSession session) {

            // Validar permisos
            if (!esAdmin(session)) {
                return ResponseEntity.status(403).body(
                    Map.of(
                        "ok", false,
                        "mensaje", "No tienes permisos de administrador."
                    )
                );
            }

            String nombreImagen = null;
            boolean actualizado = false;

            try {

                // Guardar nueva imagen si fue seleccionada
                if (imagen != null && !imagen.isEmpty()) {
                    nombreImagen = imagenProductoService.guardarImagen(imagen);
                }

                // Actualizar información del producto
                actualizado = productoAdminService.actualizarProducto(
                    idProducto,
                    producto,
                    nombreImagen
                );

                // Validar resultado
                if (!actualizado) {
                    return ResponseEntity.status(404).body(
                        Map.of(
                            "ok", false,
                            "mensaje", "No se pudo actualizar el producto."
                        )
                    );
                }

                // Respuesta exitosa
                return ResponseEntity.ok(
                    Map.of(
                        "ok", true,
                        "mensaje", "Producto actualizado correctamente."
                    )
                );

            } catch (IllegalArgumentException e) {

                return ResponseEntity.badRequest().body(
                    Map.of(
                        "ok", false,
                        "mensaje", e.getMessage()
                    )
                );

            } catch (IOException e) {

                e.printStackTrace();

                return ResponseEntity.status(500).body(
                    Map.of(
                        "ok", false,
                        "mensaje", "No se pudo almacenar la imagen."
                    )
                );

            } catch (DataAccessException e) {

                e.printStackTrace();

                return ResponseEntity.status(500).body(
                    Map.of(
                        "ok", false,
                        "mensaje", "Ocurrió un error al actualizar el producto."
                    )
                );

            } finally {

                // Eliminar la nueva imagen si no se actualizó el producto
                if (!actualizado && nombreImagen != null) {
                    imagenProductoService.eliminarImagen(nombreImagen);
                }
            }
        }
    //
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
        model.addAttribute("categorias", categorias);

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

        // VALIDAR PERMISOS
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {

            // ACTUALIZAR STOCK
            inventarioDAO.actualizarStockPorTallas(
                productoId,
                proveedores,
                tallas,
                cantidades
            );

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Stock actualizado correctamente."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );

        } catch (DataAccessException e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                "mensajeError",
                "Ocurrió un error al actualizar el inventario."
            );
        }

        return "redirect:/admin/inventario";
    }
    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("logged");
        return usuario != null && "ADMIN".equalsIgnoreCase(String.valueOf(usuario.getRol()));
    }
}