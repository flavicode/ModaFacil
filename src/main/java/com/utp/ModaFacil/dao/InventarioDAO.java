package com.utp.ModaFacil.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.utp.ModaFacil.controller.AdminController.ProductoInventario;
import com.utp.ModaFacil.controller.AdminController.ProveedorStock;
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

            List<ProveedorStock> proveedores = listarProveedoresPorProducto(idProducto);
            int stockTotal = proveedores.stream()
                    .mapToInt(ProveedorStock::getSubtotal)
                    .sum();

            return new ProductoInventario(
                    idProducto,
                    codigo,
                    nombre,
                    categoria,
                    imagenUrl,
                    stockTotal,
                    proveedores
            );
        });
    }

    public List<ProveedorStock> listarProveedoresPorProducto(Long idProducto) {
        String sql = """
            SELECT pt.id_proveedor,
                   pr.nombre AS nombre_proveedor,
                   pt.talla,
                   pt.stock
            FROM producto_tallas pt
            INNER JOIN proveedores pr ON pt.id_proveedor = pr.id_proveedor
            WHERE pt.id_producto = ?
            ORDER BY pt.id_proveedor ASC,
                CASE pt.talla
                    WHEN 'XS'  THEN 1
                    WHEN 'S'   THEN 2
                    WHEN 'M'   THEN 3
                    WHEN 'L'   THEN 4
                    WHEN 'XL'  THEN 5
                    WHEN 'XXL' THEN 6
                    ELSE 7
                END, pt.talla ASC
        """;

        Map<Long, ProveedorBuilder> mapa = new LinkedHashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long idProv = rs.getLong("id_proveedor");

            // Limitar a un máximo de 6 proveedores por producto
            if (!mapa.containsKey(idProv) && mapa.size() >= 6) {
                return;
            }

            String nombreProv = "proveedor " + idProv;
            String talla = rs.getString("talla");
            int stock = rs.getInt("stock");

            ProveedorBuilder builder = mapa.computeIfAbsent(
                    idProv,
                    k -> new ProveedorBuilder(idProv, nombreProv)
            );
            builder.tallas.add(new TallaStock(talla, stock));
            builder.subtotal += stock;
        }, idProducto);

        // Tomar las tallas del producto desde el primer proveedor encontrado
        List<String> tallasBase = new ArrayList<>();
        if (!mapa.isEmpty()) {
            for (TallaStock ts : mapa.values().iterator().next().tallas) {
                tallasBase.add(ts.getTalla());
            }
        }

        // Completar hasta 6 proveedores en memoria sin alterar la base de datos
        for (long idFijo = 1; idFijo <= 6; idFijo++) {
            if (!mapa.containsKey(idFijo)) {
                ProveedorBuilder vacio = new ProveedorBuilder(idFijo, "proveedor " + idFijo);
                for (String t : tallasBase) {
                    vacio.tallas.add(new TallaStock(t, 0));
                }
                mapa.put(idFijo, vacio);
            }
        }

        List<ProveedorStock> resultado = new ArrayList<>();
        for (ProveedorBuilder b : mapa.values()) {
            resultado.add(new ProveedorStock(b.idProv, b.nombreProv, b.subtotal, b.tallas));
        }
        return resultado;
    }

    public List<String> listarCategorias() {
        String sql = "SELECT nombre FROM categorias ORDER BY nombre ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Transactional
    public void actualizarStockPorTallas(Long idProducto, List<Long> proveedores, List<String> tallas, List<Integer> cantidades) {
        if (proveedores == null || tallas == null || cantidades == null
                || proveedores.size() != tallas.size()
                || tallas.size() != cantidades.size()) {
            return;
        }

        String sqlUpdate = "UPDATE producto_tallas SET stock = ? WHERE id_producto = ? AND id_proveedor = ? AND talla = ?";

        for (int i = 0; i < tallas.size(); i++) {
            int nuevoStock = Math.max(0, cantidades.get(i));
            jdbcTemplate.update(sqlUpdate, nuevoStock, idProducto, proveedores.get(i), tallas.get(i));
        }
    }

    private static class ProveedorBuilder {
        final Long idProv;
        final String nombreProv;
        int subtotal = 0;
        final List<TallaStock> tallas = new ArrayList<>();

        ProveedorBuilder(Long idProv, String nombreProv) {
            this.idProv = idProv;
            this.nombreProv = nombreProv;
        }
    }
}