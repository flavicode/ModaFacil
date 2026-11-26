package com.utp.ModaFacil.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Usuario;

/** Reemplaza a UsuarioRepository (lista en memoria) por la tabla usuarios. */
@Repository
public class UsuarioDAO {

    private final JdbcTemplate jdbc;

    public UsuarioDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<Usuario> MAPPER = (rs, i) -> new Usuario(
            rs.getLong("id_usuario"),
            rs.getString("nombre"),
            rs.getString("correo"),
            rs.getString("contrasena"),
            rs.getString("telefono"),
            rs.getDate("fecha_registro").toString(),
            rs.getString("rol"));

    public Usuario buscarPorCorreo(String correo) {
        try {
            return jdbc.queryForObject(
                "SELECT * FROM usuarios WHERE correo = ?", MAPPER, correo);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Usuario buscarPorId(Long id) {
        try {
            return jdbc.queryForObject(
                "SELECT * FROM usuarios WHERE id_usuario = ?", MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Inserta el usuario y le asigna el id generado por AUTO_INCREMENT. */
    public void guardar(Usuario u) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO usuarios (nombre, correo, contrasena, telefono, fecha_registro, rol) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContraseña());
            ps.setString(4, u.getTelefono());
            ps.setDate(5, java.sql.Date.valueOf(
                u.getFechaRegistro() != null ? LocalDate.parse(u.getFechaRegistro()) : LocalDate.now()));
            ps.setString(6, u.getRol());
            return ps;
        }, keys);
        if (keys.getKey() != null) u.setIdUsuario(keys.getKey().longValue());
    }

    public void actualizarTelefono(Long idUsuario, String telefono) {
        jdbc.update("UPDATE usuarios SET telefono = ? WHERE id_usuario = ?", telefono, idUsuario);
    }
}
