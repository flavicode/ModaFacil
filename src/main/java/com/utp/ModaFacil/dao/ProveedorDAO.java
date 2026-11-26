package com.utp.ModaFacil.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.controller.AdminController.ProveedorAdmin;

@Repository
public class ProveedorDAO {

    private final JdbcTemplate jdbcTemplate;

    public ProveedorDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProveedorAdmin> listarProveedores() {
        String sql = """
            SELECT pr.id_proveedor,
                   pr.nombre,
                   pr.activo,
                   COUNT(DISTINCT ofe.id_producto) AS productos_asociados
            FROM proveedores pr
            LEFT JOIN ofertas ofe ON pr.id_proveedor = ofe.id_proveedor
            GROUP BY pr.id_proveedor, pr.nombre, pr.activo
            ORDER BY pr.id_proveedor ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("id_proveedor");
            String nombreRaw = rs.getString("nombre");
            String nombre = (nombreRaw == null || nombreRaw.isBlank())
                    ? "Proveedor " + id
                    : nombreRaw.trim();
            boolean activo = rs.getBoolean("activo");
            int productosAsociados = rs.getInt("productos_asociados");

            // Valores por defecto mientras la tabla proveedores no tenga columnas RUC, contacto y teléfono
            String ruc = String.format("20%09d", id);
            String contacto = "Por asignar";
            String telefono = "—";

            return new ProveedorAdmin(
                    id,
                    nombre,
                    ruc,
                    contacto,
                    telefono,
                    productosAsociados,
                    activo
            );
        });
    }
}