package com.utp.ModaFacil.service;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.dao.UsuarioDAO;
import com.utp.ModaFacil.util.HexCrypter;

@Service
public class LoginService {

    private final UsuarioDAO usuarioDAO;

    public LoginService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String correo, String password) {

        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);

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