package com.utp.ModaFacil.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.CarritoItem;

/** Carrito persistente por usuario (tabla carrito_items). Un item = producto + importador + talla. */
@Repository
public class CarritoDAO {

    private final JdbcTemplate jdbc;

    public CarritoDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<CarritoItem> listar(Long idUsuario) {
        return jdbc.query(
            "SELECT ci.id_producto, ci.id_importador, ci.talla, ci.cantidad, " +
            "       p.nombre, p.imagen, i.nombre AS importador, o.precio " +
            "FROM carrito_items ci " +
            "JOIN productos p    ON p.id_producto = ci.id_producto " +
            "JOIN proveedores i ON i.id_importador = ci.id_importador " +
            "JOIN ofertas o      ON o.id_producto = ci.id_producto AND o.id_importador = ci.id_importador " +
            "WHERE ci.id_usuario = ? ORDER BY ci.id_producto, ci.id_importador, ci.talla",
            (rs, n) -> new CarritoItem(
                rs.getLong("id_producto"),
                rs.getLong("id_importador"),
                rs.getString("nombre"),
                rs.getString("imagen"),
                rs.getString("talla"),
                rs.getString("importador"),
                rs.getBigDecimal("precio"),
                rs.getInt("cantidad")),
            idUsuario);
    }

    /** Unidades de un producto en un importador, sumando todas las tallas (para validar stock). */
    public int cantidadReservada(Long idUsuario, Long idProducto, Long idImportador) {
        Integer c = jdbc.queryForObject(
            "SELECT COALESCE(SUM(cantidad),0) FROM carrito_items " +
            "WHERE id_usuario = ? AND id_producto = ? AND id_importador = ?",
            Integer.class, idUsuario, idProducto, idImportador);
        return c == null ? 0 : c;
    }

    /** Agrega el item o suma la cantidad si ya existe (misma talla). */
    public void agregar(Long idUsuario, Long idProducto, Long idImportador, String talla, int cantidad) {
        jdbc.update(
            "INSERT INTO carrito_items (id_usuario, id_producto, id_importador, talla, cantidad) VALUES (?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE cantidad = cantidad + VALUES(cantidad)",
            idUsuario, idProducto, idImportador, talla, cantidad);
    }

    /** cambio = +1 o -1; nunca deja la cantidad en menos de 1. */
    public void cambiarCantidad(Long idUsuario, Long idProducto, Long idImportador, String talla, int cambio) {
        jdbc.update(
            "UPDATE carrito_items SET cantidad = cantidad + ? " +
            "WHERE id_usuario = ? AND id_producto = ? AND id_importador = ? AND talla = ? AND cantidad + ? >= 1",
            cambio, idUsuario, idProducto, idImportador, talla, cambio);
    }

    public void eliminar(Long idUsuario, Long idProducto, Long idImportador, String talla) {
        jdbc.update("DELETE FROM carrito_items WHERE id_usuario = ? AND id_producto = ? AND id_importador = ? AND talla = ?",
            idUsuario, idProducto, idImportador, talla);
    }

    public void vaciar(Long idUsuario) {
        jdbc.update("DELETE FROM carrito_items WHERE id_usuario = ?", idUsuario);
    }
}
