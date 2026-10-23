package com.utp.ModaFacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.service.LoginService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("logged") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String correo,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Usuario usuario = loginService.autenticar(correo, password);

        if (usuario == null) {
            model.addAttribute("errorLogin",
                    "Correo o contraseña incorrectos.");
            return "login";
        }

        session.setAttribute("logged", usuario);

        return "redirect:/";
    }
}