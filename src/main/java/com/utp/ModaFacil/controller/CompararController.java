package com.utp.ModaFacil.controller;

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
            catalogoService.obtenerComparacionPrecios(id);

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

    @PostMapping("/comparar/carrito/agregar")
    public String agregarAlCarrito(
        @RequestParam Long idProducto,
        @RequestParam(required = false) Long idImportador,
        @RequestParam int cantidad,
        HttpSession session,
        RedirectAttributes redirect) {

            // ==========================================
            // 1. VALIDAR SESIÓN
            // ==========================================

            if (session.getAttribute("logged") == null) {
                return "redirect:/login";
            }

            String destino = "redirect:/comparar?id=" + idProducto;

            // ==========================================
            // 2. BUSCAR PRODUCTO
            // ==========================================

            Map<String, Object> producto =
                buscarProducto(idProducto);

            if (producto == null) {

                redirect.addFlashAttribute(
                    "errorCarrito",
                    "Producto no disponible."
                );

                return "redirect:/productos";
            }

            // ==========================================
            // 3. IDENTIFICAR IMPORTADOR
            // ==========================================

            // SI SE AGREGA DESDE LA TARJETA SUPERIOR,
            // UTILIZAR EL IMPORTADOR MÁS ECONÓMICO

            if (idImportador == null) {

                Map<String, Object> mejorPrecio =
                    catalogoService.obtenerMejorPrecio(idProducto);

                if (mejorPrecio == null) {

                    redirect.addFlashAttribute(
                        "errorCarrito",
                        "No existe una oferta disponible."
                    );

                    return destino;
                }

                idImportador =
                    ((Number) mejorPrecio.get("idImportador"))
                        .longValue();
            }

            // ==========================================
            // 4. OBTENER OFERTA DEL IMPORTADOR
            // ==========================================

            Map<String, Object> oferta =
                catalogoService.obtenerOfertaImportador(
                    idProducto,
                    idImportador
                );

            if (oferta == null) {

                redirect.addFlashAttribute(
                    "errorCarrito",
                    "Producto no disponible en esta tienda."
                );

                return destino;
            }

            // PRECIO DE LA TIENDA SELECCIONADA
            BigDecimal precioUnitario =
                (BigDecimal) oferta.get("precio");

            // STOCK DE LA TIENDA SELECCIONADA
            int stockTotal =
                ((Number) oferta.get("stock")).intValue();

            // ==========================================
            // 5. OBTENER CARRITO ACTUAL
            // ==========================================

            List<Map<String, Object>> carrito =
                obtenerCarrito(session);

            Map<String, Object> itemExistente = null;

            // BUSCAR POR PRODUCTO E IMPORTADOR
            for (Map<String, Object> item : carrito) {

                if (idProducto.equals(item.get("idProducto"))
                        && idImportador.equals(item.get("idImportador"))) {

                    itemExistente = item;
                    break;
                }
            }

            // ==========================================
            // 6. VALIDAR CANTIDAD DISPONIBLE
            // ==========================================

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

            // ==========================================
            // 7. AGREGAR PRODUCTO AL CARRITO
            // ==========================================

            if (itemExistente == null) {

                Map<String, Object> nuevoItem = new HashMap<>();

                nuevoItem.put("idProducto", idProducto);
                nuevoItem.put("idImportador", idImportador);
                nuevoItem.put("nombre", producto.get("nombre"));
                nuevoItem.put("cantidad", cantidad);
                nuevoItem.put("precioUnitario", precioUnitario);

                carrito.add(nuevoItem);

            } else {

                // AUMENTAR CANTIDAD EXISTENTE
                itemExistente.put(
                    "cantidad",
                    cantidadActual + cantidad
                );

                // ACTUALIZAR PRECIO DE LA TIENDA
                itemExistente.put(
                    "precioUnitario",
                    precioUnitario
                );
            }

            // ==========================================
            // 8. GUARDAR CARRITO EN LA SESIÓN
            // ==========================================

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

    // ==========================================
    // CONTAR UNIDADES POR PRODUCTO E IMPORTADOR
    // ==========================================

    private int obtenerCantidadEnCarrito(
            HttpSession session,
            Long idProducto,
            Long idImportador) {

        return obtenerCarrito(session).stream()

            // FILTRAR PRODUCTO E IMPORTADOR
            .filter(item ->
                idProducto.equals(item.get("idProducto"))
                && idImportador.equals(item.get("idImportador"))
            )

            // SUMAR CANTIDADES
            .mapToInt(item ->
                ((Number) item.get("cantidad")).intValue()
            )

            .sum();
    }
}