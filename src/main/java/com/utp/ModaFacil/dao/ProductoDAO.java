package com.utp.ModaFacil.dao;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;
import com.utp.ModaFacil.model.ProductoAdmin;
import com.utp.ModaFacil.model.ProductoDetalleAdmin;
import com.utp.ModaFacil.model.ProductoEditarAdmin;

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
    public List<String> obtenerTallasProducto(Long idProducto) {
        String sql = """
            SELECT DISTINCT talla
            FROM producto_tallas
            WHERE id_producto = ?
            ORDER BY
                CASE talla
                    WHEN 'XS' THEN 1
                    WHEN 'S' THEN 2
                    WHEN 'M' THEN 3
                    WHEN 'L' THEN 4
                    WHEN 'XL' THEN 5
                    WHEN 'XXL' THEN 6
                    ELSE 7
                END,
                talla
            """;
        return jdbc.queryForList(sql,String.class,idProducto
        );
    }
   //producto-admin4
    public Optional<ProductoDetalleAdmin> obtenerDetalleAdmin(Long idProducto) {

        String sql = """
            SELECT
                p.id_producto,
                CONCAT('P', LPAD(p.id_producto,
                    GREATEST(5, LENGTH(p.id_producto)), '0')) AS codigo,
                p.nombre,
                p.imagen,
                p.id_categoria,
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

        List<ProductoDetalleAdmin> resultado = jdbc.query(sql, (rs, i) ->
            new ProductoDetalleAdmin(
                rs.getLong("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("imagen"),
                rs.getLong("id_categoria"),
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

    //producto-admin5
    public boolean actualizarProductoAdmin(
            Long idProducto,
            ProductoEditarAdmin producto,
            String imagen) {

        String sql = """
            UPDATE productos
            SET nombre = ?,
                id_categoria = ?,
                imagen = COALESCE(?, imagen),
                descripcion = ?,
                material = ?,
                tipo_tela = ?,
                ajuste = ?,
                manga = ?,
                cuello = ?,
                genero = ?,
                ocasion = ?
            WHERE id_producto = ?
            """;

        int filasAfectadas = jdbc.update(
            sql,
            producto.nombre(),
            producto.idCategoria(),
            imagen,
            producto.descripcion(),
            producto.material(),
            producto.tipoTela(),
            producto.ajuste(),
            producto.manga(),
            producto.cuello(),
            producto.genero(),
            producto.ocasion(),
            idProducto
        );

        return filasAfectadas > 0;
    }
    //producto-admin6
    public List<Categoria> listarCategoriasPorGenero(String genero) {

        String sql = """
            SELECT c.id_categoria, c.nombre, c.color
            FROM categorias c
            INNER JOIN categoria_genero cg
                ON c.id_categoria = cg.id_categoria
            WHERE cg.genero = ?
            ORDER BY c.nombre
            """;

        return jdbc.query(sql, (rs, i) -> new Categoria(
            rs.getLong("id_categoria"),
            rs.getString("nombre"),
            rs.getString("color")
        ), genero);
    }

    //producto-admin7
    public Map<String, List<Categoria>> obtenerCategoriasPorGenero() {

        return Map.of(
            "Hombre", listarCategoriasPorGenero("Hombre"),
            "Mujer", listarCategoriasPorGenero("Mujer")
        );
    }

    // Validar categoría según el género 8
    public boolean existeCategoriaPorGenero(Long idCategoria, String genero) {
        String sql = """
            SELECT COUNT(*)
            FROM categoria_genero
            WHERE id_categoria = ?
            AND genero = ?
            """;
        Integer total = jdbc.queryForObject(
            sql,
            Integer.class,
            idCategoria,
            genero
        );
        return total != null && total > 0;
    }
}
