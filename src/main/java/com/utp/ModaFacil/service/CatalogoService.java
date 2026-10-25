package com.utp.ModaFacil.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;



@Service
public class CatalogoService {

    public List<Map<String, Object>> obtenerRelaciones() {

        Categoria polos = new Categoria(1L, "Polos", "#C65F64");
        Categoria pantalones = new Categoria(2L, "Pantalones", "#C65F64");
        Categoria vestidos = new Categoria(3L, "Vestidos", "#C65F64");

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

        List<Map<String, Object>> relaciones = new ArrayList<>();

        // POLO
        agregarRelacion(relaciones, polo, "Hombre", 1L, 39.90);
        agregarRelacion(relaciones, polo, "Hombre", 2L, 45.00);
        agregarRelacion(relaciones, polo, "Hombre", 3L, 42.00);

        // PANTALÓN
        agregarRelacion(relaciones, pantalon, "Hombre", 1L, 25.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 2L, 28.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 3L, 32.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 4L, 27.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 5L, 30.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 6L, 29.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 7L, 35.00);
        agregarRelacion(relaciones, pantalon, "Hombre", 8L, 31.00);

        // VESTIDO
        agregarRelacion(relaciones, vestido, "Mujer", 1L, 42.00);
        agregarRelacion(relaciones, vestido, "Mujer", 2L, 48.00);
        agregarRelacion(relaciones, vestido, "Mujer", 3L, 50.00);
        agregarRelacion(relaciones, vestido, "Mujer", 4L, 46.00);
        agregarRelacion(relaciones, vestido, "Mujer", 5L, 55.00);
        agregarRelacion(relaciones, vestido, "Mujer", 6L, 49.00);

        return relaciones;
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
        Map<String, Object> importador =
            (Map<String, Object>) mejorRelacion.get("importador");

        Long idImportador =
            ((Number) importador.get("idImportador")).longValue();

        Map<String, Object> resultado = new HashMap<>();

        resultado.put("precioMinimo", precioMinimo);
        resultado.put("precioMaximo", precioMaximo);
        resultado.put("ahorro", ahorro);
        resultado.put("idImportador", idImportador);
        resultado.put("importadorNombre", "Importador " + idImportador);

        return resultado;
    }


    // ==========================================
    // 2. CONVERTIR EL PRECIO A BIGDECIMAL
    // ==========================================

    private BigDecimal convertirPrecio(Object valor) {

        return BigDecimal.valueOf(((Number) valor).doubleValue())
            .setScale(2, RoundingMode.HALF_UP);
    }


    // ==========================================
    // 3. OBTENER STOCK TEMPORAL
    // ==========================================

    public int obtenerStockDemo(Long idProducto) {

        return switch (idProducto.intValue()) {
            case 1 -> 20;
            case 2 -> 15;
            case 3 -> 10;
            default -> 0;
        };
    }


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

        // INFORMACIÓN DETALLADA DEL PRODUCTO
        switch (producto.getIdProducto().intValue()) {

            case 1 -> {
                productoData.put("descripcion",
                    "Polo clásico de algodón, cómodo y versátil. Ideal para el día a día.");

                productoData.put("material", "Algodón 100%");
                productoData.put("tipoTela", "Piqué");
                productoData.put("ajuste", "Regular");
                productoData.put("manga", "Corta");
                productoData.put("cuello", "Clásico con botones");
                productoData.put("ocasion", "Casual / Diario");
                productoData.put("tallas",
                    List.of("S", "M", "L", "XL", "XXL"));
            }

            case 2 -> {
                productoData.put("descripcion",
                    "Pantalón negro de corte slim fit, moderno, cómodo y versátil para diferentes ocasiones.");

                productoData.put("material", "Algodón y elastano");
                productoData.put("tipoTela", "Denim elástico");
                productoData.put("ajuste", "Slim Fit");
                productoData.put("manga", "No aplica");
                productoData.put("cuello", "No aplica");
                productoData.put("ocasion", "Casual / Semiformal");
                productoData.put("tallas",
                    List.of("28", "30", "32", "34", "36"));
            }

            case 3 -> {
                productoData.put("descripcion",
                    "Vestido casual de verano, ligero y cómodo, ideal para días cálidos y ocasiones informales.");

                productoData.put("material", "Algodón y poliéster");
                productoData.put("tipoTela", "Tela ligera");
                productoData.put("ajuste", "Regular");
                productoData.put("manga", "Corta");
                productoData.put("cuello", "Redondo");
                productoData.put("ocasion", "Casual / Verano");
                productoData.put("tallas",
                    List.of("XS", "S", "M", "L", "XL"));
            }

            default -> {
                productoData.put("descripcion",
                    "Sin descripción disponible");
                productoData.put("material", "No especificado");
                productoData.put("tipoTela", "No especificado");
                productoData.put("ajuste", "No especificado");
                productoData.put("manga", "No aplica");
                productoData.put("cuello", "No aplica");
                productoData.put("ocasion", "No especificada");
                productoData.put("tallas", List.of());
            }
        }

        // CATEGORÍA
        Map<String, Object> categoriaData = new HashMap<>();
        categoriaData.put(
            "nombre",
            producto.getCategoria().getNombre()
        );

        productoData.put("categoria", categoriaData);

        // IMPORTADOR
        Map<String, Object> importadorData = new HashMap<>();
        importadorData.put("idImportador", idImportador);

        // RELACIÓN PRODUCTO - IMPORTADOR - PRECIO
        Map<String, Object> relacion = new HashMap<>();
        relacion.put("producto", productoData);
        relacion.put("importador", importadorData);
        relacion.put("precio", precio);

        relaciones.add(relacion);

    }
}