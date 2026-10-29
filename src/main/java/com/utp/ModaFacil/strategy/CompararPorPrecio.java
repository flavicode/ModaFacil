
package com.utp.ModaFacil.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CompararPorPrecio implements EstrategiaComparacion {

    @Override
    public String obtenerCriterio() {
        return "precio";
    }

    @Override
    public List<Map<String, Object>> ordenar(
            List<Map<String, Object>> tiendas) {

        return tiendas.stream()
            .sorted(Comparator.comparing(
                tienda -> (BigDecimal) tienda.get("precio")
            ))
            .toList();
    }
}
