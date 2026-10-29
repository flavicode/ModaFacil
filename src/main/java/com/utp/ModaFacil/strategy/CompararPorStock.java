
package com.utp.ModaFacil.strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CompararPorStock implements EstrategiaComparacion {

    @Override
    public String obtenerCriterio() {
        return "stock";
    }

    @Override
    public List<Map<String, Object>> ordenar(
            List<Map<String, Object>> tiendas) {

        return tiendas.stream()
            .sorted(Comparator.comparingInt(
                (Map<String, Object> tienda) ->
                    ((Number) tienda.get("stock")).intValue()
            ).reversed())
            .toList();
    }
}
