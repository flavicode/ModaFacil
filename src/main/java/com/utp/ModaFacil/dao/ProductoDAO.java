package com.utp.ModaFacil.dao;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Categoria;
import com.utp.ModaFacil.model.Producto;

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
}
