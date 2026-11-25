package com.utp.ModaFacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.dao.UsuarioDAO;
import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.util.HexCrypter;

import jakarta.servlet.http.HttpSession;

@Controller
public class RegistroController {

    private final UsuarioDAO usuarioDAO;

    public RegistroController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping("/registro")
    public String registro(HttpSession session) {
        if (session.getAttribute("logged") != null) {
            return "redirect:/";
        }
        return "registro";
    }

    // Registro de usuarios
    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        nombre = nombre.trim();
        correo = correo.trim();
        password = password.trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "errorRegistro",
                "Los datos enviados no cumplen con los requisitos de seguridad."
            );
            return "redirect:/login?registrar=true";
        }

        if (usuarioDAO.buscarPorCorreo(correo) != null) {
            redirectAttributes.addFlashAttribute(
                "errorRegistro",
                "El correo ya se encuentra registrado. Intenta con otro."
            );
            return "redirect:/login?registrar=true";
        }

        String passwordCifrada = HexCrypter.encrypt(password);
        String fechaActual = java.time.LocalDate.now().toString();

        // el id lo genera MySQL (AUTO_INCREMENT)

        Usuario nuevoUsuario = new Usuario(
            null,
            nombre,
            correo,
            passwordCifrada,
            null,
            fechaActual,
            "USUARIO"
        );

        usuarioDAO.guardar(nuevoUsuario);

        return "redirect:/login?registrado=true";
    }

    // Verificar correo registrado
    @GetMapping("/registro/verificar-correo")
    @ResponseBody
    public boolean verificarCorreo(@RequestParam String correo) {
        return usuarioDAO.buscarPorCorreo(correo.trim()) != null;
    }
}