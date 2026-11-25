package com.utp.ModaFacil.dao;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;
import com.utp.ModaFacil.model.ProductoAdmin;

/** Acceso a las tablas categorias y productos. */
@Repository
public class ProductoDAO {

    private final JdbcTemplate jdbc;

    public ProductoDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final String SELECT =
        "SELECT p.id_producto, p.nombre, p.imagen, c.id_categoria, c.nombre AS cat_nombre, c.color " +
        "FROM productos p JOIN categorias c ON c.id_categoria = p.id_categoria ";

    private static final RowMapper<Producto> MAPPER = (rs, i) -> new Producto(
            rs.getLong("id_producto"),
            new Categoria(rs.getLong("id_categoria"), rs.getString("cat_nombre"), rs.getString("color")),
            rs.getString("nombre"),
            rs.getString("imagen"));

    public List<Producto> listar() {
        return jdbc.query(SELECT + "ORDER BY p.id_producto", MAPPER);
    }

    public List<Producto> listarPorCategoria(Long idCategoria) {
        return jdbc.query(SELECT + "WHERE c.id_categoria = ? ORDER BY p.id_producto", MAPPER, idCategoria);
    }

    public Producto buscarPorId(Long id) {
        try {
            return jdbc.queryForObject(SELECT + "WHERE p.id_producto = ?", MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Categoria> listarCategorias() {
        return jdbc.query("SELECT * FROM categorias ORDER BY id_categoria",
            (rs, i) -> new Categoria(rs.getLong("id_categoria"), rs.getString("nombre"), rs.getString("color")));
    }
    //producto-admin1
    public List<ProductoAdmin> buscarProductosAdmin(
        String busqueda, String estado) {

        String sql = """
            SELECT
                p.id_producto,
                CONCAT('P', LPAD(p.id_producto,
                    GREATEST(5, LENGTH(p.id_producto)), '0')) AS codigo,
                p.nombre,
                p.imagen,
                c.nombre AS categoria,
                o.precio,
                COALESCE(o.stock, 0) AS stock,
                p.activo
            FROM productos p
            INNER JOIN categorias c
                ON p.id_categoria = c.id_categoria
            LEFT JOIN (
                SELECT
                    ofe.id_producto,
                    MIN(CASE
                        WHEN ofe.stock > 0 THEN ofe.precio
                    END) AS precio,
                    SUM(ofe.stock) AS stock
                FROM ofertas ofe
                INNER JOIN proveedores pr
                    ON ofe.id_importador = pr.id_importador
                WHERE pr.activo = TRUE
                GROUP BY ofe.id_producto
            ) o ON p.id_producto = o.id_producto
            WHERE (
                ? = ''
                OR LOCATE(LOWER(?), LOWER(p.nombre)) > 0
                OR LOCATE(
                    LOWER(?),
                    LOWER(CONCAT('P', LPAD(p.id_producto,
                        GREATEST(5, LENGTH(p.id_producto)), '0')))
                ) > 0
            )
            AND (
                ? = 'todos'
                OR (? = 'activo' AND p.activo = TRUE)
                OR (? = 'inactivo' AND p.activo = FALSE)
            )
            ORDER BY p.id_producto DESC
            """;

        return jdbc.query(
            sql,
            (rs, i) -> new ProductoAdmin(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("imagen"),
                rs.getString("categoria"),
                rs.getBigDecimal("precio"),
                rs.getLong("stock"),
                rs.getBoolean("activo")
            ),
            busqueda,busqueda,busqueda,estado,estado,estado
        );
    }

    public long contarProductosAdmin() {
        String sql = "SELECT COUNT(*) FROM productos";
        Long total = jdbc.queryForObject(sql, Long.class);
        return total != null ? total : 0;
    }

    //producto-admin2
    public boolean actualizarEstado(Long idProducto, boolean activo) {
        String sql = """
            UPDATE productos
            SET activo = ?
            WHERE id_producto = ?
            """;
        int filasAfectadas = jdbc.update(sql,activo,idProducto);
        return filasAfectadas > 0;
    }
}
