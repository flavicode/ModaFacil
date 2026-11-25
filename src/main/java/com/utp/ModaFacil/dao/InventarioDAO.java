package com.utp.ModaFacil.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.utp.ModaFacil.controller.AdminController.ProductoInventario;
import com.utp.ModaFacil.controller.AdminController.TallaStock;

@Repository
public class InventarioDAO {

    private final JdbcTemplate jdbcTemplate;

    public InventarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductoInventario> listarInventario() {
        String sqlProductos = """
            SELECT p.id_producto,
                   p.nombre,
                   p.imagen,
                   c.nombre AS categoria
            FROM productos p
            INNER JOIN categorias c ON p.id_categoria = c.id_categoria
            WHERE p.activo = TRUE
            ORDER BY p.id_producto DESC
        """;

        return jdbcTemplate.query(sqlProductos, (rs, rowNum) -> {
            Long idProducto = rs.getLong("id_producto");
            String codigo = String.format("P%05d", idProducto);
            String nombre = rs.getString("nombre");
            String categoria = rs.getString("categoria");

            String img = rs.getString("imagen");
            String imagenUrl = (img == null || img.isBlank())
                    ? "/img/logo.png"
                    : (img.startsWith("/") || img.startsWith("http") ? img : "/img/" + img);

            List<TallaStock> tallas = listarTallasPorProducto(idProducto);
            int stockTotal = tallas.stream()
                    .mapToInt(TallaStock::getStock)
                    .sum();

            return new ProductoInventario(
                    idProducto,
                    codigo,
                    nombre,
                    categoria,
                    imagenUrl,
                    stockTotal,
                    tallas
            );
        });
    }

    public List<TallaStock> listarTallasPorProducto(Long idProducto) {
        String sqlTallas = """
            SELECT talla, stock
            FROM producto_tallas
            WHERE id_producto = ?
            ORDER BY CASE talla
                WHEN 'XS'  THEN 1
                WHEN 'S'   THEN 2
                WHEN 'M'   THEN 3
                WHEN 'L'   THEN 4
                WHEN 'XL'  THEN 5
                WHEN 'XXL' THEN 6
                ELSE 7
            END, talla ASC
        """;

        return jdbcTemplate.query(
                sqlTallas,
                (rs, rowNum) -> new TallaStock(rs.getString("talla"), rs.getInt("stock")),
                idProducto
        );
    }

    public List<String> listarCategorias() {
        String sql = "SELECT nombre FROM categorias ORDER BY nombre ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Transactional
    public void actualizarStockPorTallas(Long idProducto, List<String> tallas, List<Integer> cantidades) {
        if (tallas == null || cantidades == null || tallas.size() != cantidades.size()) {
            return;
        }

        String sqlUpdate = "UPDATE producto_tallas SET stock = ? WHERE id_producto = ? AND talla = ?";

        for (int i = 0; i < tallas.size(); i++) {
            int nuevoStock = Math.max(0, cantidades.get(i));
            jdbcTemplate.update(sqlUpdate, nuevoStock, idProducto, tallas.get(i));
        }
    }
}