package com.utp.ModaFacil.advice;

import com.utp.ModaFacil.model.CarritoItem;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;

@ControllerAdvice
public class CarritoModelAdvice {

    @ModelAttribute("carritoItems")
    public List<CarritoItem> carritoItems(HttpSession session) {

        Object carrito = session.getAttribute("carrito");

        if (carrito instanceof List<?> lista) {
            return lista.stream()
                    .filter(CarritoItem.class::isInstance)
                    .map(CarritoItem.class::cast)
                    .toList();
        }

        return List.of();
    }

    @ModelAttribute("carritoSubtotal")
    public BigDecimal carritoSubtotal(
            @ModelAttribute("carritoItems") List<CarritoItem> items) {

        return items.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @ModelAttribute("carritoCantidad")
    public int carritoCantidad(
            @ModelAttribute("carritoItems") List<CarritoItem> items) {

        return items.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }
}