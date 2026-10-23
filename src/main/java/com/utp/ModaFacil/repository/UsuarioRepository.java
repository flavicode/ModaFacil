package com.utp.ModaFacil.repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.util.HexCrypter;

@Repository
public class UsuarioRepository {

    private final List<Usuario> usuarios =
            new CopyOnWriteArrayList<>();

    public UsuarioRepository() {
        usuarios.add(new Usuario(
            1L, "Diego León", "diegoleon6431@gmail.com",
            HexCrypter.encrypt("Dieg_025"),
            "987654321", "2026-08-22"
        ));

        usuarios.add(new Usuario(
            2L, "Thirsa Alvarez", "thirsa18utp@gmail.com",
            HexCrypter.encrypt("Thirsa_18"),
            "999888777", "2026-09-14"
        ));

        usuarios.add(new Usuario(
            3L, "Dana Quiñones", "dana@gmail.com",
            HexCrypter.encrypt("Danae_26"),
            "987654567", "2026-10-01"
        ));
    }


    public synchronized Long obtenerSiguienteId() {
        return usuarios.stream()
                .mapToLong(Usuario::getIdUsuario)
                .max()
                .orElse(0L) + 1;
    }

    public void guardar(Usuario usuario) {
        usuarios.add(usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarios.stream()
                .filter(u -> u.getCorreo()
                        .equalsIgnoreCase(correo))
                .findFirst()
                .orElse(null);
    }
}