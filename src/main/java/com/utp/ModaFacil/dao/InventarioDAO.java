package com.utp.ModaFacil.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.utp.ModaFacil.model.ProductoInventario;
import com.utp.ModaFacil.model.ProveedorStock;
import com.utp.ModaFacil.model.TallaStock;

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
    //modificar
    public List<ProveedorStock> listarProveedoresPorProducto(Long idProducto) {

        String sql = """
            SELECT o.id_proveedor,
                pr.nombre AS nombre_proveedor,
                t.talla,
                COALESCE(pt.stock, 0) AS stock
            FROM ofertas o
            INNER JOIN proveedores pr
                ON o.id_proveedor = pr.id_proveedor
            LEFT JOIN (
                SELECT DISTINCT talla
                FROM producto_tallas
                WHERE id_producto = ?
            ) t ON 1 = 1
            LEFT JOIN producto_tallas pt
                ON pt.id_producto = o.id_producto
                AND pt.id_proveedor = o.id_proveedor
                AND pt.talla = t.talla
            WHERE o.id_producto = ?
            ORDER BY o.id_proveedor,
                CASE UPPER(TRIM(t.talla))
                    WHEN 'XS' THEN 1
                    WHEN 'S' THEN 2
                    WHEN 'M' THEN 3
                    WHEN 'L' THEN 4
                    WHEN 'XL' THEN 5
                    WHEN 'XXL' THEN 6
                    ELSE 7
                END,
                CASE
                    WHEN t.talla REGEXP '^[0-9]+$'
                    THEN CAST(t.talla AS UNSIGNED)
                END,
                t.talla
            """;

        Map<Long, ProveedorBuilder> mapa = new LinkedHashMap<>();

        jdbcTemplate.query(sql, rs -> {

            Long idProv = rs.getLong("id_proveedor");
            String nombreProv = "Proveedor " + idProv;
            String talla = rs.getString("talla");
            int stock = rs.getInt("stock");

            ProveedorBuilder builder = mapa.computeIfAbsent(
                idProv,
                k -> new ProveedorBuilder(idProv, nombreProv)
            );

            if (talla != null) {
                builder.tallas.add(new TallaStock(talla, stock));
                builder.subtotal += stock;
            }

        }, idProducto, idProducto);

        List<ProveedorStock> resultado = new ArrayList<>();

        for (ProveedorBuilder b : mapa.values()) {
            resultado.add(new ProveedorStock(
                b.idProv,
                b.nombreProv,
                b.subtotal,
                b.tallas
            ));
        }

        return resultado;
    }

    public List<String> listarCategorias() {
        String sql = "SELECT nombre FROM categorias ORDER BY nombre ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    
    //
    @Transactional
    public void actualizarStockPorTallas(
            Long idProducto,
            List<Long> proveedores,
            List<String> tallas,
            List<Integer> cantidades) {

        // 1. VALIDAR INFORMACIÓN RECIBIDA
        if (idProducto == null ||
            proveedores == null ||
            tallas == null ||
            cantidades == null ||
            proveedores.isEmpty() ||
            proveedores.size() != tallas.size() ||
            tallas.size() != cantidades.size()) {

            throw new IllegalArgumentException(
                "La información del inventario no es válida."
            );
        }

        // 2. VALIDAR PRODUCTO ACTIVO
        List<Long> productos = jdbcTemplate.queryForList(
            """
            SELECT id_producto
            FROM productos
            WHERE id_producto = ? AND activo = TRUE
            FOR UPDATE
            """,
            Long.class,
            idProducto
        );

        if (productos.isEmpty()) {
            throw new IllegalArgumentException(
                "El producto no está disponible en el inventario."
            );
        }

        // 3. OBTENER PROVEEDORES AUTORIZADOS
        Set<Long> proveedoresValidos = new HashSet<>(
            jdbcTemplate.queryForList(
                """
                SELECT id_proveedor
                FROM ofertas
                WHERE id_producto = ?
                """,
                Long.class,
                idProducto
            )
        );

        // 4. OBTENER TALLAS VÁLIDAS DEL PRODUCTO
        Set<String> tallasValidas = new HashSet<>(
            jdbcTemplate.queryForList(
                """
                SELECT DISTINCT talla
                FROM producto_tallas
                WHERE id_producto = ?
                """,
                String.class,
                idProducto
            )
        );

        // 5. CONSULTA PARA INSERTAR O ACTUALIZAR
        String sql = """
            INSERT INTO producto_tallas
                (id_producto, id_proveedor, talla, stock)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE stock = ?
            """;

        Set<String> combinaciones = new HashSet<>();

        // 6. PROCESAR CADA COMBINACIÓN
        for (int i = 0; i < tallas.size(); i++) {

            Long idProveedor = proveedores.get(i);
            String talla = tallas.get(i);
            Integer stock = cantidades.get(i);

            // Validar proveedor
            if (!proveedoresValidos.contains(idProveedor)) {
                throw new IllegalArgumentException(
                    "El proveedor no tiene una oferta registrada."
                );
            }

            // Validar talla
            if (!tallasValidas.contains(talla)) {
                throw new IllegalArgumentException(
                    "La talla seleccionada no corresponde al producto."
                );
            }

            // Validar cantidad
            if (stock == null || stock < 0) {
                throw new IllegalArgumentException(
                    "El stock no puede ser negativo."
                );
            }

            // Evitar combinaciones duplicadas
            String clave = idProveedor + ":" + talla;

            if (!combinaciones.add(clave)) {
                throw new IllegalArgumentException(
                    "Se encontraron registros de stock duplicados."
                );
            }

            // Guardar stock
            jdbcTemplate.update(
                sql,
                idProducto,
                idProveedor,
                talla,
                stock,
                stock
            );
        }
    }

    private static class ProveedorBuilder {
        Long idProv;
        String nombreProv;
        int subtotal = 0;
        List<TallaStock> tallas = new ArrayList<>();

        ProveedorBuilder(Long idProv, String nombreProv) {
            this.idProv = idProv;
            this.nombreProv = nombreProv;
        }
    }
}