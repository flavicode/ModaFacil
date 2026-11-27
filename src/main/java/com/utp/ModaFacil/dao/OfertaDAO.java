package com.utp.ModaFacil.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Ofertas = producto + proveedor + precio.
 * El stock ahora se calcula y descuenta desde producto_tallas, manteniendo
 * el mismo formato de Map para no romper controllers ni .js.
 */
@Repository
public class OfertaDAO {

    private final JdbcTemplate jdbc;

    public OfertaDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

        public List<Map<String, Object>> obtenerRelaciones() {
        // 1. Cargamos tallas por producto (para el catálogo) y por (producto + proveedor)
        Map<Long, Set<String>> tallasPorProducto = new HashMap<>();
        Map<String, Set<String>> tallasPorOferta = new HashMap<>();
        Map<String, Integer> stockPorOferta = new HashMap<>();

        String sqlTallas = """
            SELECT id_producto, id_proveedor, talla, stock
            FROM producto_tallas
            WHERE stock > 0
            ORDER BY id_producto, id_proveedor,
                CASE UPPER(TRIM(talla))
                    WHEN 'XS'  THEN 1
                    WHEN 'S'   THEN 2
                    WHEN 'M'   THEN 3
                    WHEN 'L'   THEN 4
                    WHEN 'XL'  THEN 5
                    WHEN 'XXL' THEN 6
                    ELSE 7
                END,
                CAST(talla AS UNSIGNED) ASC,
                talla ASC
            """;

        jdbc.query(sqlTallas, rs -> {
            Long idProd = rs.getLong("id_producto");
            Long idProv = rs.getLong("id_proveedor");
            String talla = rs.getString("talla");
            int stock = rs.getInt("stock");
            String claveOferta = idProd + "-" + idProv;

            tallasPorProducto.computeIfAbsent(idProd, k -> new LinkedHashSet<>()).add(talla);
            tallasPorOferta.computeIfAbsent(claveOferta, k -> new LinkedHashSet<>()).add(talla);
            stockPorOferta.merge(claveOferta, stock, Integer::sum);
        });

        // 2. Consultamos SOLO las ofertas (producto + proveedor) que tengan stock > 0
        return jdbc.query(
            "SELECT p.*, c.nombre AS categoria, o.id_proveedor, i.nombre AS nombre_proveedor, o.precio " +
            "FROM ofertas o " +
            "JOIN productos p   ON p.id_producto   = o.id_producto " +
            "JOIN categorias c  ON c.id_categoria  = p.id_categoria " +
            "JOIN proveedores i ON i.id_proveedor = o.id_proveedor AND i.activo = TRUE " +
            "WHERE p.activo = TRUE " +
            "  AND EXISTS ( " +
            "      SELECT 1 FROM producto_tallas pt " +
            "      WHERE pt.id_producto = o.id_producto " +
            "        AND pt.id_proveedor = o.id_proveedor " +
            "        AND pt.stock > 0 " +
            "  ) " +
            "ORDER BY p.id_producto, o.id_proveedor",
            (rs, n) -> {
                Long idProducto = rs.getLong("id_producto");
                Long idProveedor = rs.getLong("id_proveedor");
                String claveOferta = idProducto + "-" + idProveedor;

                Map<String, Object> producto = new HashMap<>();
                producto.put("idProducto", idProducto);
                producto.put("nombre", rs.getString("nombre"));
                producto.put("imagen", rs.getString("imagen"));
                producto.put("genero", rs.getString("genero"));
                producto.put("descripcion", rs.getString("descripcion"));
                producto.put("material", rs.getString("material"));
                producto.put("tipoTela", rs.getString("tipo_tela"));
                producto.put("ajuste", rs.getString("ajuste"));
                producto.put("manga", rs.getString("manga"));
                producto.put("cuello", rs.getString("cuello"));
                producto.put("ocasion", rs.getString("ocasion"));
                producto.put("tallas", ordenarTallas(tallasPorProducto.getOrDefault(idProducto, Set.of())));
                producto.put("categoria", Map.of("nombre", rs.getString("categoria")));

                Map<String, Object> proveedor = new HashMap<>();
                proveedor.put("idProveedor", idProveedor);
                proveedor.put("nombre", rs.getString("nombre_proveedor"));

                Map<String, Object> relacion = new HashMap<>();
                relacion.put("producto", producto);
                relacion.put("proveedor", proveedor);
                relacion.put("precio", rs.getBigDecimal("precio"));
                relacion.put("stock", stockPorOferta.getOrDefault(claveOferta, 0));
                relacion.put("tallasOferta", new ArrayList<>(tallasPorOferta.getOrDefault(claveOferta, Set.of())));
                return relacion;
            });
    }

    private List<String> ordenarTallas(Set<String> tallasSet) {
        List<String> ordenLetras = List.of("XS", "S", "M", "L", "XL", "XXL");
        List<String> lista = new ArrayList<>(tallasSet);

        lista.sort((a, b) -> {
            String t1 = a.trim().toUpperCase();
            String t2 = b.trim().toUpperCase();
            int idx1 = ordenLetras.indexOf(t1);
            int idx2 = ordenLetras.indexOf(t2);

            if (idx1 != -1 && idx2 != -1) return Integer.compare(idx1, idx2);
            if (idx1 != -1) return -1;
            if (idx2 != -1) return 1;

            try {
                return Integer.compare(Integer.parseInt(t1), Integer.parseInt(t2));
            } catch (NumberFormatException e) {
                return t1.compareTo(t2);
            }
        });

        return lista;
    }

    /** Equivale a CatalogoService.obtenerOfertaProveedor(...). */
    public Map<String, Object> obtenerOferta(Long idProducto, Long idProveedor) {
        try {
            return jdbc.queryForObject(
                "SELECT o.precio, COALESCE(( " +
                "    SELECT SUM(pt.stock) FROM producto_tallas pt " +
                "    WHERE pt.id_producto = o.id_producto AND pt.id_proveedor = o.id_proveedor " +
                "), 0) AS stock " +
                "FROM ofertas o " +
                "WHERE o.id_producto = ? AND o.id_proveedor = ?",
                (rs, n) -> {
                    Map<String, Object> oferta = new HashMap<>();
                    oferta.put("idProducto", idProducto);
                    oferta.put("idProveedor", idProveedor);
                    oferta.put("precio", rs.getBigDecimal("precio"));
                    oferta.put("stock", rs.getInt("stock"));
                    return oferta;
                }, idProducto, idProveedor);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    /** Descuenta stock de una talla específica en producto_tallas; devuelve false si no alcanza. */
    public boolean descontarStockPorTalla(Long idProducto, Long idProveedor, String talla, int cantidad) {
        return jdbc.update(
            "UPDATE producto_tallas SET stock = stock - ? WHERE id_producto = ? AND id_proveedor = ? AND talla = ? AND stock >= ?",
            cantidad, idProducto, idProveedor, talla, cantidad) == 1;
    }


    /** Mantiene compatibilidad si algún servicio antiguo llama descontarStock sin talla. */
    public boolean descontarStock(Long idProducto, Long idProveedor, int cantidad) {
        return jdbc.update(
            "UPDATE producto_tallas SET stock = stock - ? " +
            "WHERE id_producto = ? AND stock >= ? " +
            "ORDER BY stock DESC LIMIT 1",
            cantidad, idProducto, cantidad) == 1;
    }

    public BigDecimal obtenerPrecio(Long idProducto, Long idProveedor) {
        Map<String, Object> o = obtenerOferta(idProducto, idProveedor);
        return o == null ? null : (BigDecimal) o.get("precio");
    }
    /** Obtiene el stock disponible de una talla específica para un proveedor. */
    public int obtenerStockPorTalla(Long idProducto, Long idProveedor, String talla) {
        try {
            Integer stock = jdbc.queryForObject(
                "SELECT stock FROM producto_tallas WHERE id_producto = ? AND id_proveedor = ? AND talla = ?",
                Integer.class, idProducto, idProveedor, talla);
            return stock != null ? stock : 0;
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

}
