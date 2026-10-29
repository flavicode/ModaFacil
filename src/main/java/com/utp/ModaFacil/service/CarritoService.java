package com.utp.ModaFacil.service;

import com.utp.ModaFacil.model.CarritoItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {

    @SuppressWarnings("unchecked")
    public List<CarritoItem> obtenerItems(HttpSession session) {
        Object carrito = session.getAttribute("carrito");
        if (carrito instanceof List<?>) {
            return (List<CarritoItem>) carrito;
        }
        return new ArrayList<>();
    }

    public BigDecimal calcularSubtotal(HttpSession session) {
        List<CarritoItem> items = obtenerItems(session);
        return items.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
