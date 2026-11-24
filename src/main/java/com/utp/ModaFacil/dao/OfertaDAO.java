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
 * Ofertas = producto + importador + precio + stock.
 * obtenerRelaciones() devuelve el MISMO formato de Map que hoy arma
 * CatalogoService, así que los controllers y los .js no cambian.
 */
@Repository
public class OfertaDAO {

    private final JdbcTemplate jdbc;

    public OfertaDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> obtenerRelaciones() {
        // tallas de todos los productos en una sola consulta
        Map<Long, List<String>> tallas = new HashMap<>();
        jdbc.query("SELECT id_producto, talla FROM producto_tallas ORDER BY id_producto, talla", rs -> {
            tallas.computeIfAbsent(rs.getLong("id_producto"), k -> new ArrayList<>())
                  .add(rs.getString("talla"));
        });

        return jdbc.query(
            "SELECT p.*, c.nombre AS categoria, o.id_importador, o.precio, o.stock " +
            "FROM ofertas o " +
            "JOIN productos p   ON p.id_producto  = o.id_producto " +
            "JOIN categorias c  ON c.id_categoria = p.id_categoria " +
            "JOIN importadores i ON i.id_importador = o.id_importador AND i.activo " +
            "ORDER BY p.id_producto, o.id_importador",
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
                relacion.put("importador", Map.of("idImportador", rs.getLong("id_importador")));
                relacion.put("precio", rs.getBigDecimal("precio"));
                relacion.put("stock", rs.getInt("stock"));
                return relacion;
            });
    }

    /** Equivale a CatalogoService.obtenerOfertaImportador(...). */
    public Map<String, Object> obtenerOferta(Long idProducto, Long idImportador) {
        try {
            return jdbc.queryForObject(
                "SELECT precio, stock FROM ofertas WHERE id_producto = ? AND id_importador = ?",
                (rs, n) -> {
                    Map<String, Object> oferta = new HashMap<>();
                    oferta.put("idProducto", idProducto);
                    oferta.put("idImportador", idImportador);
                    oferta.put("precio", rs.getBigDecimal("precio"));
                    oferta.put("stock", rs.getInt("stock"));
                    return oferta;
                }, idProducto, idImportador);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Descuenta stock solo si alcanza; devuelve false si no había suficiente. */
    public boolean descontarStock(Long idProducto, Long idImportador, int cantidad) {
        return jdbc.update(
            "UPDATE ofertas SET stock = stock - ? WHERE id_producto = ? AND id_importador = ? AND stock >= ?",
            cantidad, idProducto, idImportador, cantidad) == 1;
    }

    public BigDecimal obtenerPrecio(Long idProducto, Long idImportador) {
        Map<String, Object> o = obtenerOferta(idProducto, idImportador);
        return o == null ? null : (BigDecimal) o.get("precio");
    }
}
