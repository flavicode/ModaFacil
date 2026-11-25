package com.utp.ModaFacil.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 1. Cargamos tallas y sumamos el stock total por producto en una sola consulta
        Map<Long, List<String>> tallas = new HashMap<>();
        Map<Long, Integer> stockPorProducto = new HashMap<>();

        jdbc.query("SELECT id_producto, talla, stock FROM producto_tallas ORDER BY id_producto, talla", rs -> {
            Long idProd = rs.getLong("id_producto");
            tallas.computeIfAbsent(idProd, k -> new ArrayList<>())
                  .add(rs.getString("talla"));
            stockPorProducto.merge(idProd, rs.getInt("stock"), Integer::sum);
        });

        // 2. Consultamos ofertas sin o.stock (ya que no existe en la tabla ofertas)
        return jdbc.query(
            "SELECT p.*, c.nombre AS categoria, o.id_proveedor, o.precio " +
            "FROM ofertas o " +
            "JOIN productos p   ON p.id_producto   = o.id_producto " +
            "JOIN categorias c  ON c.id_categoria  = p.id_categoria " +
            "JOIN proveedores i ON i.id_proveedor = o.id_proveedor AND i.activo = TRUE " +
            "WHERE p.activo = TRUE " +
            "ORDER BY p.id_producto, o.id_proveedor",
            (rs, n) -> {
                Long idProducto = rs.getLong("id_producto");

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
                producto.put("tallas", tallas.getOrDefault(idProducto, List.of()));
                producto.put("categoria", Map.of("nombre", rs.getString("categoria")));

                Map<String, Object> relacion = new HashMap<>();
                relacion.put("producto", producto);
                relacion.put("proveedor", Map.of("idProveedor", rs.getLong("id_proveedor")));
                relacion.put("precio", rs.getBigDecimal("precio"));
                relacion.put("stock", stockPorProducto.getOrDefault(idProducto, 0));
                return relacion;
            });
    }

    /** Equivale a CatalogoService.obtenerOfertaProveedor(...). */
    public Map<String, Object> obtenerOferta(Long idProducto, Long idProveedor) {
        try {
            return jdbc.queryForObject(
                "SELECT o.precio, COALESCE((SELECT SUM(pt.stock) FROM producto_tallas pt WHERE pt.id_producto = o.id_producto), 0) AS stock " +
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
    public boolean descontarStockPorTalla(Long idProducto, String talla, int cantidad) {
        return jdbc.update(
            "UPDATE producto_tallas SET stock = stock - ? WHERE id_producto = ? AND talla = ? AND stock >= ?",
            cantidad, idProducto, talla, cantidad) == 1;
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
}
