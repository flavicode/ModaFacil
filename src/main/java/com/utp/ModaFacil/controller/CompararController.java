
package com.utp.ModaFacil.controller;

import com.utp.ModaFacil.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CompararController {

    private final CatalogoService catalogoService;

    public CompararController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/comparar")
    public String mostrarComparacion(
            @RequestParam(required = false) Long id,
            Model model,
            HttpSession session) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaActual", "comparar");

        if (id == null) {
            return "redirect:/productos";
        }

        Map<String, Object> producto = buscarProducto(id);

        if (producto == null) {
            return "redirect:/productos";
        }

        Map<String, Object> mejorPrecio =
            catalogoService.obtenerMejorPrecio(id);

        if (mejorPrecio == null) {
            return "redirect:/productos";
        }

        int stockTotal = catalogoService.obtenerStockDemo(id);
        int cantidadEnCarrito = obtenerCantidadEnCarrito(session, id);
        int stockDisponible = Math.max(0, stockTotal - cantidadEnCarrito);

        model.addAttribute("productoSeleccionado", producto);
        model.addAttribute("mejorPrecio", mejorPrecio);
        model.addAttribute("stockDisponible", stockDisponible);

        // CÓDIGO TEMPORAL DEL PRODUCTO
        String codigo = String.format("PBN-%03d", id);
        model.addAttribute("codigoProducto", codigo);

        return "comparar";
    }

    @PostMapping("/comparar/carrito/agregar")
    public String agregarAlCarrito(
            @RequestParam Long idProducto,
            @RequestParam int cantidad,
            HttpSession session,
            RedirectAttributes redirect) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        String destino = "redirect:/comparar?id=" + idProducto;

        Map<String, Object> producto =
            buscarProducto(idProducto);

        Map<String, Object> mejorPrecio =
            catalogoService.obtenerMejorPrecio(idProducto);

        if (producto == null || mejorPrecio == null) {
            redirect.addFlashAttribute(
                "errorCarrito", "Producto no disponible."
            );
            return "redirect:/productos";
        }

        int stockTotal =
            catalogoService.obtenerStockDemo(idProducto);

        List<Map<String, Object>> carrito =
            obtenerCarrito(session);

        Map<String, Object> itemExistente = null;

        for (Map<String, Object> item : carrito) {
            if (idProducto.equals(item.get("idProducto"))) {
                itemExistente = item;
                break;
            }
        }

        int cantidadActual = itemExistente == null
            ? 0
            : ((Number) itemExistente.get("cantidad")).intValue();

        int disponible = stockTotal - cantidadActual;

        if (cantidad < 1 || cantidad > disponible) {
            redirect.addFlashAttribute(
                "errorCarrito",
                "Cantidad inválida o superior al stock disponible."
            );
            return destino;
        }

        BigDecimal precioUnitario =
            (BigDecimal) mejorPrecio.get("precioMinimo");

        if (itemExistente == null) {

            Map<String, Object> nuevoItem = new HashMap<>();

            nuevoItem.put("idProducto", idProducto);
            nuevoItem.put("nombre", producto.get("nombre"));
            nuevoItem.put("cantidad", cantidad);
            nuevoItem.put("precioUnitario", precioUnitario);
            nuevoItem.put(
                "idImportador",
                mejorPrecio.get("idImportador")
            );

            carrito.add(nuevoItem);

        } else {

            itemExistente.put(
                "cantidad",
                cantidadActual + cantidad
            );

            // ACTUALIZA EL PRECIO AL MOMENTO DE AGREGAR.
            itemExistente.put("precioUnitario", precioUnitario);
            itemExistente.put(
                "idImportador",
                mejorPrecio.get("idImportador")
            );
        }

        session.setAttribute("carrito", carrito);

        redirect.addFlashAttribute(
            "mensajeCarrito",
            "Producto agregado al carrito correctamente."
        );

        return destino;
    }

    // BUSCAR PRODUCTO SIN REPETIR LA LÓGICA
    @SuppressWarnings("unchecked")
    private Map<String, Object> buscarProducto(Long id) {

        for (Map<String, Object> relacion :
                catalogoService.obtenerRelaciones()) {

            Map<String, Object> producto =
                (Map<String, Object>) relacion.get("producto");

            if (producto != null
                    && id.equals(producto.get("idProducto"))) {
                return producto;
            }
        }

        return null;
    }

    // OBTENER CARRITO DE LA SESIÓN
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> obtenerCarrito(
            HttpSession session) {

        Object carrito = session.getAttribute("carrito");

        if (carrito instanceof List<?>) {
            return (List<Map<String, Object>>) carrito;
        }

        return new ArrayList<>();
    }

    // CONTAR UNIDADES DEL PRODUCTO YA AGREGADAS
    private int obtenerCantidadEnCarrito(
            HttpSession session,
            Long idProducto) {

        return obtenerCarrito(session).stream()
            .filter(item -> idProducto.equals(item.get("idProducto")))
            .mapToInt(item ->
                ((Number) item.get("cantidad")).intValue()
            )
            .sum();
    }
}
