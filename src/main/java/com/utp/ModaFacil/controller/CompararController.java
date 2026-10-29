package com.utp.ModaFacil.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.utp.ModaFacil.model.CarritoItem;
import com.utp.ModaFacil.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CompararController {

    private final CatalogoService catalogoService;

    public CompararController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/comparar")
    public String mostrarComparacion(
        @RequestParam(required = false) Long id,
        @RequestParam(defaultValue = "precio") String criterio,
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
        // ==========================================
        // OBTENER COMPARACIÓN DE PRECIOS
        // ==========================================

        // OBTENER TODAS LAS TIENDAS DEL PRODUCTO
        List<Map<String, Object>> tiendasComparacion =
            catalogoService.obtenerComparacionPrecios(id, criterio);

        // ENVIAR LISTA DE TIENDAS AL HTML
        model.addAttribute(
            "tiendasComparacion",
            tiendasComparacion
        );

        // ENVIAR CANTIDAD TOTAL DE TIENDAS
        model.addAttribute(
            "cantidadTiendas",
            tiendasComparacion.size()
        );

        // ==========================================
        // STOCK DEL IMPORTADOR CON MEJOR PRECIO
        // ==========================================

        // OBTENER ID DEL IMPORTADOR MÁS ECONÓMICO
        Long idMejorImportador =
            ((Number) mejorPrecio.get("idImportador")).longValue();

        // OBTENER OFERTA DEL IMPORTADOR
        Map<String, Object> ofertaMejorPrecio =
            catalogoService.obtenerOfertaImportador(
                id,
                idMejorImportador
            );

        // OBTENER STOCK DEL IMPORTADOR
        int stockTotal = ofertaMejorPrecio == null
            ? 0
            : ((Number) ofertaMejorPrecio.get("stock")).intValue();

        // CONTAR CANTIDADES QUE YA ESTÁN EN EL CARRITO
        int cantidadEnCarrito =
            obtenerCantidadEnCarrito(
                session,
                id,
                idMejorImportador
            );

        // CALCULAR STOCK DISPONIBLE
        int stockDisponible =
            Math.max(0, stockTotal - cantidadEnCarrito);

        model.addAttribute("productoSeleccionado", producto);
        model.addAttribute("mejorPrecio", mejorPrecio);
        model.addAttribute("stockDisponible", stockDisponible);

        // CÓDIGO TEMPORAL DEL PRODUCTO
        String codigo = String.format("PBN-%03d", id);
        model.addAttribute("codigoProducto", codigo);

        return "comparar";
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
    private List<CarritoItem> obtenerCarrito(HttpSession session) {

        Object carrito = session.getAttribute("carrito");

        if (carrito instanceof List<?>) {
            return (List<CarritoItem>) carrito;
        }

        return new ArrayList<>();
    }


    // CONTAR UNIDADES POR PRODUCTO E IMPORTADOR

    
    private int obtenerCantidadEnCarrito(
            HttpSession session,
            Long idProducto,
            Long idImportador) {

        return obtenerCarrito(session).stream()

                // FILTRAR POR PRODUCTO E IMPORTADOR
                .filter(item ->
                        idProducto.equals(item.getIdProducto())
                        && idImportador.equals(item.getIdImportador())
                )

                // SUMAR CANTIDADES DE TODAS LAS TALLAS
                .mapToInt(CarritoItem::getCantidad)

                .sum();
    }
}