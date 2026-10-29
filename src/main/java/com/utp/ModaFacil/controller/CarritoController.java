package com.utp.ModaFacil.controller;

import com.utp.ModaFacil.model.CarritoItem;
import com.utp.ModaFacil.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/comparar/carrito")
public class CarritoController {

    private final CatalogoService catalogoService;

    public CarritoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // ==========================================
    // OBTENER CARRITO DE LA SESIÓN
    // ==========================================

    @SuppressWarnings("unchecked")
    private List<CarritoItem> obtenerCarrito(HttpSession session) {

        Object carrito = session.getAttribute("carrito");

        if (carrito instanceof List<?>) {
            return (List<CarritoItem>) carrito;
        }

        List<CarritoItem> nuevoCarrito = new ArrayList<>();
        session.setAttribute("carrito", nuevoCarrito);

        return nuevoCarrito;
    }

    // ==========================================
    // CONTAR UNIDADES POR PRODUCTO E IMPORTADOR
    // INCLUYE TODAS LAS TALLAS
    // ==========================================

    private int cantidadReservada(
            List<CarritoItem> carrito,
            Long idProducto,
            Long idImportador) {

        return carrito.stream()
                .filter(item ->
                        Objects.equals(item.getIdProducto(), idProducto)
                        && Objects.equals(item.getIdImportador(), idImportador))
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }

    // ==========================================
    // AGREGAR PRODUCTO AL CARRITO
    // ==========================================

    @PostMapping("/agregar")
    public String agregar(
            @RequestParam Long idProducto,
            @RequestParam Long idImportador,
            @RequestParam String talla,
            @RequestParam(defaultValue = "1") int cantidad,
            HttpSession session,
            RedirectAttributes redirect) {

        // VALIDAR SESIÓN
        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        System.out.println("===== AGREGAR AL CARRITO =====");
        System.out.println("Producto: " + idProducto);
        System.out.println("Importador: " + idImportador);
        System.out.println("Talla: " + talla);
        System.out.println("Cantidad: " + cantidad);

        String destino = "redirect:/comparar?id=" + idProducto;

        // OBTENER OFERTA REAL DEL IMPORTADOR
        Map<String, Object> oferta =
                catalogoService.obtenerOfertaImportador(
                        idProducto, idImportador);

        if (oferta == null) {
            redirect.addFlashAttribute(
                    "errorCarrito",
                    "Producto no disponible en esta tienda."
            );
            return destino;
        }

        // OBTENER PRODUCTO DESDE LAS RELACIONES
        Map<String, Object> producto = null;

        for (Map<String, Object> relacion :
                catalogoService.obtenerRelaciones()) {

            @SuppressWarnings("unchecked")
            Map<String, Object> datosProducto =
                    (Map<String, Object>) relacion.get("producto");

            if (datosProducto != null &&
                    Objects.equals(
                            datosProducto.get("idProducto"), idProducto)) {

                producto = datosProducto;
                break;
            }
        }

        if (producto == null) {
            redirect.addFlashAttribute(
                    "errorCarrito", "Producto no encontrado."
            );
            return destino;
        }

        // VALIDAR TALLA
        @SuppressWarnings("unchecked")
        List<String> tallas =
                (List<String>) producto.get("tallas");

        if (talla == null ||
                tallas == null ||
                !tallas.contains(talla)) {

            redirect.addFlashAttribute(
                    "errorCarrito", "Selecciona una talla válida."
            );
            return destino;
        }

        // PRECIO Y STOCK DE LA TIENDA SELECCIONADA
        BigDecimal precio =
                (BigDecimal) oferta.get("precio");

        int stock =
                ((Number) oferta.get("stock")).intValue();

        List<CarritoItem> carrito = obtenerCarrito(session);

        // VALIDAR STOCK CONSIDERANDO TODAS LAS TALLAS
        int reservado = cantidadReservada(
                carrito, idProducto, idImportador);

        if (cantidad < 1 ||
                reservado + (long) cantidad > stock) {

            redirect.addFlashAttribute(
                    "errorCarrito",
                    "Cantidad inválida o superior al stock disponible."
            );
            return destino;
        }

        // BUSCAR MISMO PRODUCTO, IMPORTADOR Y TALLA
        CarritoItem existente = carrito.stream()
                .filter(item ->
                        Objects.equals(item.getIdProducto(), idProducto)
                        && Objects.equals(item.getIdImportador(), idImportador)
                        && Objects.equals(item.getTalla(), talla))
                .findFirst()
                .orElse(null);

        if (existente != null) {

            existente.setCantidad(
                    existente.getCantidad() + cantidad
            );

        } else {

            CarritoItem nuevo = new CarritoItem(
                    idProducto,
                    idImportador,
                    (String) producto.get("nombre"),
                    (String) producto.get("imagen"),
                    talla,
                    "Importador " + idImportador,
                    precio,
                    cantidad
            );

            carrito.add(nuevo);
        }

        redirect.addFlashAttribute(
                "mensajeCarrito",
                "Producto agregado al carrito correctamente."
        );

        return destino + "&carrito=abierto";
    }

    // ==========================================
    // AUMENTAR O DISMINUIR CANTIDAD
    // ==========================================

    @PostMapping("/cantidad")
    public String cambiarCantidad(
            @RequestParam Long idProducto,
            @RequestParam Long idImportador,
            @RequestParam String talla,
            @RequestParam int cambio,
            HttpSession session) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        String destino = "redirect:/comparar?id="
                + idProducto + "&carrito=abierto";

        if (cambio != 1 && cambio != -1) {
            return destino;
        }

        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = carrito.stream()
                .filter(actual ->
                        Objects.equals(actual.getIdProducto(), idProducto)
                        && Objects.equals(actual.getIdImportador(), idImportador)
                        && Objects.equals(actual.getTalla(), talla))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return destino;
        }

        Map<String, Object> oferta =
                catalogoService.obtenerOfertaImportador(
                        idProducto, idImportador);

        if (oferta == null) {
            return destino;
        }

        int stock =
                ((Number) oferta.get("stock")).intValue();

        int nuevaCantidad = item.getCantidad() + cambio;

        int reservado = cantidadReservada(
                carrito, idProducto, idImportador);

        // VALIDAR EL STOCK TOTAL DEL IMPORTADOR
        if (nuevaCantidad >= 1 &&
                reservado + cambio <= stock) {

            item.setCantidad(nuevaCantidad);
        }

        return destino;
    }
}
