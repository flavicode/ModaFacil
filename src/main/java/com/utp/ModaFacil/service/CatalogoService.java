package com.utp.ModaFacil.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.dao.OfertaDAO;
import com.utp.ModaFacil.strategy.EstrategiaComparacion;

@Service
public class CatalogoService {
    private final List<EstrategiaComparacion> estrategias;
    private final OfertaDAO ofertaDAO;

    public CatalogoService(
            List<EstrategiaComparacion> estrategias,
            OfertaDAO ofertaDAO) {
        this.estrategias = estrategias;
        this.ofertaDAO = ofertaDAO;
    }

    // Las relaciones producto - proveedor - precio - stock ahora vienen de MySQL
    public List<Map<String, Object>> obtenerRelaciones() {
        return ofertaDAO.obtenerRelaciones();
    }


    // ==========================================
    // 1. OBTENER EL MEJOR PRECIO DEL PRODUCTO
    // ==========================================

    public Map<String, Object> obtenerMejorPrecio(Long idProducto) {

        List<Map<String, Object>> relacionesProducto =
            obtenerRelaciones().stream()
                .filter(relacion -> {

                    @SuppressWarnings("unchecked")
                    Map<String, Object> producto =
                        (Map<String, Object>) relacion.get("producto");

                    return producto != null
                        && idProducto.equals(producto.get("idProducto"));
                })
                .filter(relacion -> relacion.get("precio") instanceof Number)
                .toList();

        if (relacionesProducto.isEmpty()) {
            return null;
        }

        Map<String, Object> mejorRelacion =
            relacionesProducto.stream()
                .min(Comparator.comparing(
                    relacion -> convertirPrecio(relacion.get("precio"))
                ))
                .orElseThrow();

        Map<String, Object> mayorRelacion =
            relacionesProducto.stream()
                .max(Comparator.comparing(
                    relacion -> convertirPrecio(relacion.get("precio"))
                ))
                .orElseThrow();

        BigDecimal precioMinimo =
            convertirPrecio(mejorRelacion.get("precio"));

        BigDecimal precioMaximo =
            convertirPrecio(mayorRelacion.get("precio"));

        BigDecimal ahorro =
            precioMaximo.subtract(precioMinimo);

        @SuppressWarnings("unchecked")
        Map<String, Object> proveedor =
            (Map<String, Object>) mejorRelacion.get("proveedor");

        Long idProveedor =
            ((Number) proveedor.get("idProveedor")).longValue();

        Map<String, Object> resultado = new HashMap<>();

        resultado.put("precioMinimo", precioMinimo);
        resultado.put("precioMaximo", precioMaximo);
        resultado.put("ahorro", ahorro);
        resultado.put("idProveedor", idProveedor);
        resultado.put("proveedorNombre", "Proveedor " + idProveedor);

        return resultado;
    }

    // ==========================================
    // OBTENER TIENDAS PARA COMPARAR PRECIOS
    // ==========================================

    public List<Map<String, Object>> obtenerComparacionPrecios(
        Long idProducto) {
    return obtenerComparacionPrecios(idProducto, "precio");
    }

    public List<Map<String, Object>> obtenerComparacionPrecios(
        Long idProducto, String criterio) {

    Map<Long, Map<String, Object>> tiendasUnicas =
        new LinkedHashMap<>();

    for (Map<String, Object> relacion : obtenerRelaciones()) {

        @SuppressWarnings("unchecked")
        Map<String, Object> producto =
            (Map<String, Object>) relacion.get("producto");

        // FILTRAR SOLO EL PRODUCTO SELECCIONADO
        if (producto == null ||
            !idProducto.equals(producto.get("idProducto"))) {
            continue;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> proveedor =
            (Map<String, Object>) relacion.get("proveedor");

        if (proveedor == null) {
            continue;
        }

        Long idProveedor =
            ((Number) proveedor.get("idProveedor")).longValue();

        BigDecimal precio =
            convertirPrecio(relacion.get("precio"));

        int stock =
            ((Number) relacion.get("stock")).intValue();

        // EVITAR proveedores DUPLICADOS
        // SI EXISTEN DOS RELACIONES, CONSERVAR EL MENOR PRECIO
        Map<String, Object> tiendaExistente =
            tiendasUnicas.get(idProveedor);

        if (tiendaExistente != null) {

            BigDecimal precioExistente =
                (BigDecimal) tiendaExistente.get("precio");

            if (precio.compareTo(precioExistente) >= 0) {
                continue;
            }
        }

        // DETERMINAR ESTADO DEL STOCK
        String estadoStock;
        String claseStock;

        if (stock > 10) {
            estadoStock = "En stock";
            claseStock = "stock-alto";

        } else if (stock >= 2) {
            estadoStock = "Pocas unidades";
            claseStock = "stock-bajo";

        } else if (stock == 1) {
            estadoStock = "Solo queda una unidad";
            claseStock = "stock-ultimo";

        } else {
            estadoStock = "Agotado";
            claseStock = "stock-sin";
        }

        // CREAR INFORMACIÓN DEL PROVEEDOR
        Map<String, Object> tienda = new HashMap<>();

        tienda.put("idProveedor", idProveedor);
        tienda.put("nombre", "Proveedor " + idProveedor);
        tienda.put("precio", precio);
        tienda.put("tallas", producto.get("tallas"));
        tienda.put("tallasDisponibles", relacion.getOrDefault("tallasOferta", List.of()));
        tienda.put("stock", stock);
        tienda.put("estadoStock", estadoStock);
        tienda.put("claseStock", claseStock);

        // GUARDAR UNA SOLA VEZ POR PROVEEDOR
        tiendasUnicas.put(idProveedor, tienda);
    }
    
    // SELECCIONAR LA ESTRATEGIA DE COMPARACIÓN
    String criterioElegido =
        criterio == null ? "" : criterio.toLowerCase().trim();

    EstrategiaComparacion estrategia = estrategias.stream()
        .filter(e -> e.obtenerCriterio().equals(criterioElegido))
        .findFirst()
        .orElseGet(() -> estrategias.stream()
            .filter(e -> e.obtenerCriterio().equals("precio"))
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    "No existe una estrategia para ordenar por precio"
                )
            )
        );

    // ORDENAR LAS TIENDAS CON LA ESTRATEGIA ELEGIDA
    return estrategia.ordenar(
        new ArrayList<>(tiendasUnicas.values())
    );
}


    // ==========================================
    // 2. CONVERTIR EL PRECIO A BIGDECIMAL
    // ==========================================

    private BigDecimal convertirPrecio(Object valor) {

        return BigDecimal.valueOf(((Number) valor).doubleValue())
            .setScale(2, RoundingMode.HALF_UP);
    }



    // ==========================================
    // BUSCAR PRODUCTO EN UN PROVEEDOR ESPECÍFICO
    // ==========================================

    public Map<String, Object> obtenerOfertaProveedor(
            Long idProducto,
            Long idProveedor) {
        return ofertaDAO.obtenerOferta(idProducto, idProveedor);
    }

}