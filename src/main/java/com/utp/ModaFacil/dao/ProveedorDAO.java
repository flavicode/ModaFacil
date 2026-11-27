package com.utp.ModaFacil.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Proveedor;

@Repository
public class ProveedorDAO {

    private final JdbcTemplate jdbcTemplate;

    public ProveedorDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Proveedor> listarProveedores(String busqueda) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.id_proveedor,
                   p.nombre,
                   p.ruc,
                   p.contacto,
                   p.telefono,
                   COUNT(DISTINCT o.id_producto) AS productos_asociados
            FROM proveedores p
            LEFT JOIN ofertas o ON o.id_proveedor = p.id_proveedor
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (busqueda != null && !busqueda.isBlank()) {
            String like = "%" + busqueda.trim().toLowerCase() + "%";
            sql.append("""
                AND (
                    LOWER(p.nombre) LIKE ?
                    OR LOWER(COALESCE(p.ruc, '')) LIKE ?
                    OR LOWER(COALESCE(p.contacto, '')) LIKE ?
                )
                """);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append("""
            GROUP BY p.id_proveedor, p.nombre, p.ruc, p.contacto, p.telefono
            ORDER BY p.id_proveedor ASC
            """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapProveedor(rs), params.toArray());
    }

    public Optional<Proveedor> buscarPorId(Long idProveedor) {
        String sql = """
            SELECT p.id_proveedor,
                   p.nombre,
                   p.ruc,
                   p.contacto,
                   p.telefono,
                   COUNT(DISTINCT o.id_producto) AS productos_asociados
            FROM proveedores p
            LEFT JOIN ofertas o ON o.id_proveedor = p.id_proveedor
            WHERE p.id_proveedor = ?
            GROUP BY p.id_proveedor, p.nombre, p.ruc, p.contacto, p.telefono
            """;

        List<Proveedor> lista = jdbcTemplate.query(sql, (rs, rowNum) -> mapProveedor(rs), idProveedor);
        return lista.stream().findFirst();
    }

    public int registrar(Proveedor proveedor) {
        String sql = """
            INSERT INTO proveedores (nombre, ruc, contacto, telefono)
            VALUES (?, ?, ?, ?)
            """;
        return jdbcTemplate.update(
            sql,
            proveedor.getNombre(),
            proveedor.getRuc(),
            proveedor.getContacto(),
            proveedor.getTelefono()
        );
    }

    public int actualizar(Proveedor proveedor) {
        String sql = """
            UPDATE proveedores
            SET nombre = ?, ruc = ?, contacto = ?, telefono = ?
            WHERE id_proveedor = ?
            """;
        return jdbcTemplate.update(
            sql,
            proveedor.getNombre(),
            proveedor.getRuc(),
            proveedor.getContacto(),
            proveedor.getTelefono(),
            proveedor.getIdProveedor()
        );
    }

    public int eliminar(Long idProveedor) {
        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";
        return jdbcTemplate.update(sql, idProveedor);
    }

    private Proveedor mapProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getLong("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setRuc(rs.getString("ruc"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setProductosAsociados(rs.getInt("productos_asociados"));
        return p;
    }
}