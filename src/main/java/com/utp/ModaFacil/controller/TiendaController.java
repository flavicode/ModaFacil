package com.utp.ModaFacil.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.ModaFacil.model.Usuario;
import com.utp.ModaFacil.util.HexCrypter;

import jakarta.servlet.http.HttpSession;

@Controller
public class TiendaController implements ErrorController {

    //base de datos de prueba
    private static List<Usuario> dbUsuarios = new ArrayList<>();

    static {
        String hashDiego = HexCrypter.encrypt("Dieg_025");
        String hashThirsa = HexCrypter.encrypt("Thirsa_18");
        String hashDana = HexCrypter.encrypt("Danae_26");

        // TABLA: USUARIO
        Usuario u1 = new Usuario(1L, "Diego León", "diegoleon6431@gmail.com", hashDiego, "987654321", "2026-08-22");
        Usuario u2 = new Usuario(2L, "Thirsa Alvarez", "thirsa18utp@gmail.com", hashThirsa, "999888777", "2026-09-14");
        Usuario u3 = new Usuario(3L, "Dana Quiñones", "dana@gmail.com", hashDana, "987654567", "2026-10-01");


        dbUsuarios.add(u1);
        dbUsuarios.add(u2);
        dbUsuarios.add(u3);

    }

    //Vistas publicas
    @GetMapping("/")
    public String inicio() {
        return "inicio"; 
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto"; 
    }

    //Vistas exclusivas de Invitado
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("logged") != null){
            return "redirect:/";
        }
        return "login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, @RequestParam String password,
        HttpSession session, Model model) {
        
        Usuario usuarioEncontrado = null;
        for (Usuario u : dbUsuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo)) {
                usuarioEncontrado = u;
                break;
            }
        }
        if (usuarioEncontrado == null) {
            model.addAttribute("errorLogin", "El correo electrónico no está registrado.");
            return "login";
        }
        
        if (HexCrypter.checkMatch(password, usuarioEncontrado.getContraseña())) {
            session.setAttribute("logged", usuarioEncontrado);
            return "redirect:/";
        } else {
            model.addAttribute("errorLogin", "Contraseña incorrecta. Inténtalo de nuevo.");
            return "login";
        }
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre, @RequestParam String correo,
        @RequestParam String password, RedirectAttributes redirectAttributes) {
        nombre = nombre.trim();
        correo = correo.trim();
        password = password.trim();

        if (nombre.isEmpty() || correo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorRegistro", "Los datos enviados no cumplen con los requisitos de seguridad.");
            return "redirect:/login?registrar=true";
        }

        for (Usuario u : dbUsuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo)) {
                redirectAttributes.addFlashAttribute("errorRegistro", "El correo ya se encuentra registrado. Intenta con otro.");
                return "redirect:/login?registrar=true";
            }
        }

        String passwordCifrada = HexCrypter.encrypt(password);
        Long nuevoId = (long) (dbUsuarios.size() + 1);
        String fechaActual = java.time.LocalDate.now().toString();

        Usuario nuevoUsuario = new Usuario(nuevoId, nombre, correo, passwordCifrada, null, fechaActual);
        dbUsuarios.add(nuevoUsuario);
        
        return "redirect:/login?registrado=true"; 
    }
    
    @GetMapping("/registro/verificar-correo")
    @ResponseBody
    public boolean verificarCorreo(@RequestParam String correo) {
        for (Usuario u : dbUsuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo.trim())) {
                return true;
            }
        }
        return false;
    }

    //Vistas exclusivas de User
    @GetMapping("/productos")
    public String productos(Model model, HttpSession session) {
        if (session.getAttribute("logged") == null){
            return "redirect:/login";
        }
        /*
        model.addAttribute("categorias",dbCategorias);
        model.addAttribute("listaRelaciones", dbProdModadores);
        */
        return "productos"; 
    }

    @GetMapping("/comparar")
    public String comparar(@RequestParam(required = false) Long id, Model model, HttpSession session) {
        if (session.getAttribute("logged") == null){
            return "redirect:/login";
        }
        if (id == null){
            model.addAttribute("productoSeleccionado",null);
            return "comparar";
        }
/*
        Producto productoEncontrado = dbProductos.stream()
            .filter(p -> p.getIdProducto().equals(id)).findFirst().orElse(null);
        if (productoEncontrado == null){
            model.addAttribute("productoSeleccionado",null);
            return "comparar";
        }

        List<ProdModador> ofertasDelProducto = new ArrayList<>();
        double precioMinimo = Double.MAX_VALUE;

        for (ProdModador rel : dbProdModadores){
            if (rel.getProducto().getIdProducto().equals(id)){
                ofertasDelProducto.add(rel);
                if (rel.getPrecio() < precioMinimo){
                    precioMinimo = rel.getPrecio();
                }
            }
        }

        model.addAttribute("productoSeleccionado", productoEncontrado);
        model.addAttribute("ofertas", ofertasDelProducto);
        model.addAttribute("precioMinimo", precioMinimo == Double.MAX_VALUE ? 0.0 : precioMinimo);
        */
        return "comparar"; 
    }

    //Redirecciones
    @GetMapping("/logout")
    public  String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping("/error")
    public String manejarError() {
        return "error404";
    }

    //Para visualizar el layout
    @GetMapping("/layout")
    public  String layout(){
        return "layout";
    }
}
