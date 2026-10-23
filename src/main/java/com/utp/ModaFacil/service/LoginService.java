package com.utp.ModaFacil.service;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.repository.UsuarioRepository;
import com.utp.ModaFacil.util.HexCrypter;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;

    public LoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario autenticar(String correo, String password) {

        Usuario usuario = usuarioRepository.buscarPorCorreo(correo);

        if (usuario == null) {
            return null;
        }

        if (!HexCrypter.checkMatch(
                password, usuario.getContraseña())) {
            return null;
        }

        return usuario;
    }
}