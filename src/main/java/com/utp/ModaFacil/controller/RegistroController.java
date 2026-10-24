package com.utp.ModaFacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.repository.UsuarioRepository;
import com.utp.ModaFacil.util.HexCrypter;

import jakarta.servlet.http.HttpSession;

@Controller
public class RegistroController {

    private final UsuarioRepository usuarioRepository;

    public RegistroController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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

        if (usuarioRepository.buscarPorCorreo(correo) != null) {
            redirectAttributes.addFlashAttribute(
                "errorRegistro",
                "El correo ya se encuentra registrado. Intenta con otro."
            );
            return "redirect:/login?registrar=true";
        }

        String passwordCifrada = HexCrypter.encrypt(password);
        String fechaActual = java.time.LocalDate.now().toString();

        Long nuevoId = usuarioRepository.obtenerSiguienteId();

        Usuario nuevoUsuario = new Usuario(
            nuevoId,
            nombre,
            correo,
            passwordCifrada,
            null,
            fechaActual
        );

        usuarioRepository.guardar(nuevoUsuario);

        return "redirect:/login?registrado=true";
    }

    // Verificar correo registrado
    @GetMapping("/registro/verificar-correo")
    @ResponseBody
    public boolean verificarCorreo(@RequestParam String correo) {
        return usuarioRepository.buscarPorCorreo(correo.trim()) != null;
    }
}