package com.utp.ModaFacil.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.model.CarritoItem;
import com.utp.ModaFacil.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

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
    // CONTAR UNIDADES POR PRODUCTO, PROVEEDOR Y TALLA
    // ==========================================
    private int cantidadReservadaPorTalla(
            List<CarritoItem> carrito,
            Long idProducto,
            Long idProveedor,
            String talla) {

        return carrito.stream()
                .filter(item ->
                        Objects.equals(item.getIdProducto(), idProducto)
                        && Objects.equals(item.getIdProveedor(), idProveedor)
                        && Objects.equals(item.getTalla(), talla))
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }

    // ==========================================
    // AGREGAR PRODUCTO AL CARRITO
    // ==========================================
    @PostMapping("/agregar")
    public String agregar(
            @RequestParam Long idProducto,
            @RequestParam Long idProveedor,
            @RequestParam String talla,
            @RequestParam(defaultValue = "1") int cantidad,
            HttpSession session,
            RedirectAttributes redirect) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        String destino = "redirect:/comparar?id=" + idProducto;

        // OBTENER OFERTA REAL DEL PROVEEDOR
        Map<String, Object> oferta =
                catalogoService.obtenerOfertaProveedor(idProducto, idProveedor);

        if (oferta == null) {
            redirect.addFlashAttribute("errorCarrito", "Producto no disponible en esta tienda.");
            return destino;
        }

        // OBTENER PRODUCTO DESDE LAS RELACIONES
        Map<String, Object> producto = null;
        for (Map<String, Object> relacion : catalogoService.obtenerRelaciones()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> datosProducto = (Map<String, Object>) relacion.get("producto");

            if (datosProducto != null && Objects.equals(datosProducto.get("idProducto"), idProducto)) {
                producto = datosProducto;
                break;
            }
        }

        if (producto == null) {
            redirect.addFlashAttribute("errorCarrito", "Producto no encontrado.");
            return destino;
        }

        // VALIDAR TALLA
        @SuppressWarnings("unchecked")
        List<String> tallas = (List<String>) producto.get("tallas");

        if (talla == null || tallas == null || !tallas.contains(talla)) {
            redirect.addFlashAttribute("errorCarrito", "Selecciona una talla válida.");
            return destino;
        }

        // PRECIO Y STOCK ESPECÍFICO DE ESA TALLA EN ESE PROVEEDOR
        BigDecimal precio = (BigDecimal) oferta.get("precio");
        int stockTalla = catalogoService.obtenerStockPorTalla(idProducto, idProveedor, talla);

        List<CarritoItem> carrito = obtenerCarrito(session);
        int reservado = cantidadReservadaPorTalla(carrito, idProducto, idProveedor, talla);

        if (cantidad < 1 || reservado + (long) cantidad > stockTalla) {
            redirect.addFlashAttribute(
                    "errorCarrito",
                    "Cantidad inválida o superior al stock disponible para la talla " + talla + "."
            );
            return destino;
        }

        // BUSCAR MISMO PRODUCTO, PROVEEDOR Y TALLA
        CarritoItem existente = carrito.stream()
                .filter(item ->
                        Objects.equals(item.getIdProducto(), idProducto)
                        && Objects.equals(item.getIdProveedor(), idProveedor)
                        && Objects.equals(item.getTalla(), talla))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            CarritoItem nuevo = new CarritoItem(
                    idProducto,
                    idProveedor,
                    (String) producto.get("nombre"),
                    (String) producto.get("imagen"),
                    talla,
                    "Proveedor " + idProveedor,
                    precio,
                    cantidad
            );
            carrito.add(nuevo);
        }

        redirect.addFlashAttribute("mensajeCarrito", "Producto agregado al carrito correctamente.");
        return destino + "&carrito=abierto";
    }

    // ==========================================
    // AUMENTAR O DISMINUIR CANTIDAD
    // ==========================================
    @PostMapping("/cantidad")
    public String cambiarCantidad(
            @RequestParam Long idProducto,
            @RequestParam Long idProveedor,
            @RequestParam String talla,
            @RequestParam int cambio,
            HttpSession session) {

        if (session.getAttribute("logged") == null) {
            return "redirect:/login";
        }

        String destino = "redirect:/comparar?id=" + idProducto + "&carrito=abierto";

        if (cambio != 1 && cambio != -1) {
            return destino;
        }

        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = carrito.stream()
                .filter(actual ->
                        Objects.equals(actual.getIdProducto(), idProducto)
                        && Objects.equals(actual.getIdProveedor(), idProveedor)
                        && Objects.equals(actual.getTalla(), talla))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return destino;
        }

        // VALIDAR EL STOCK REAL DE ESA TALLA EN ESE PROVEEDOR
        int stockTalla = catalogoService.obtenerStockPorTalla(idProducto, idProveedor, talla);
        int nuevaCantidad = item.getCantidad() + cambio;
        int reservado = cantidadReservadaPorTalla(carrito, idProducto, idProveedor, talla);

        if (nuevaCantidad >= 1 && reservado + cambio <= stockTalla) {
            item.setCantidad(nuevaCantidad);
        }

        return destino;
    }
}
