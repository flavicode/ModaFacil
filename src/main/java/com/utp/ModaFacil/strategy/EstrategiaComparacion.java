
package com.utp.ModaFacil.strategy;

import java.util.List;
import java.util.Map;

public interface EstrategiaComparacion {

    String obtenerCriterio();

    List<Map<String, Object>> ordenar(
        List<Map<String, Object>> tiendas
    );
}
