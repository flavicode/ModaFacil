package com.utp.ModaFacil.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;
import com.utp.ModaFacil.model.ProductoAdmin;
import com.utp.ModaFacil.model.ProductoDetalleAdmin;

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


    public long contarProductosAdmin() {
        String sql = "SELECT COUNT(*) FROM productos";
        Long total = jdbc.queryForObject(sql, Long.class);
        return total != null ? total : 0;
    }

    //producto-admin1
    public List<ProductoAdmin> buscarProductosAdmin(String busqueda, String estado) {
        String sql = """
            SELECT
                p.id_producto,
                CONCAT('P', LPAD(p.id_producto,
                    GREATEST(5, LENGTH(p.id_producto)), '0')) AS codigo,
                p.nombre,
                p.imagen,
                c.nombre AS categoria,
                p.activo

            FROM productos p
            INNER JOIN categorias c
                ON p.id_categoria = c.id_categoria
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
        return jdbc.query(sql,(rs, i) -> new ProductoAdmin(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("imagen"),
                rs.getString("categoria"),
                rs.getBoolean("activo")
            ),
            busqueda,busqueda,busqueda,estado,estado,estado
        );
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

    //producto-admin3
    private List<String> obtenerTallasProducto(Long idProducto) {
        String sql = """
            SELECT DISTINCT t.nombre
            FROM producto_tallas pt
            INNER JOIN tallas t
                ON pt.id_talla = t.id_talla
            WHERE pt.id_producto = ?
            """;
        return jdbc.query(sql,(rs, i) -> rs.getString("nombre"),idProducto
        );
    }
   //producto-admin4
    public Optional<ProductoDetalleAdmin> obtenerDetalleAdmin(
        Long idProducto) {

        String sql = """
            SELECT
                p.id_producto,
                CONCAT('P', LPAD(p.id_producto,
                    GREATEST(5, LENGTH(p.id_producto)), '0')) AS codigo,
                p.nombre,
                p.imagen,
                c.nombre AS categoria,
                p.activo,
                p.descripcion,
                p.material,
                p.tipo_tela,
                p.ajuste,
                p.manga,
                p.cuello,
                p.genero,
                p.ocasion
            FROM productos p
            INNER JOIN categorias c
                ON p.id_categoria = c.id_categoria
            WHERE p.id_producto = ?
            """;
        List<String> tallas = obtenerTallasProducto(idProducto);
        List<ProductoDetalleAdmin> resultado = jdbc.query(sql,(rs, i) -> new ProductoDetalleAdmin(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("imagen"),
                rs.getString("categoria"),
                rs.getBoolean("activo"),
                rs.getString("descripcion"),
                rs.getString("material"),
                rs.getString("tipo_tela"),
                rs.getString("ajuste"),
                rs.getString("manga"),
                rs.getString("cuello"),
                rs.getString("genero"),
                rs.getString("ocasion"),
                tallas
            ),
            idProducto
        );
        return resultado.stream().findFirst();
    }
}
