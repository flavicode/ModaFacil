package com.utp.ModaFacil.model;

import java.math.BigDecimal;

public record ProductoAdmin(
    Long idProducto,
    String codigo,
    String nombre,
    String imagen,
    String categoria,
    BigDecimal precio,
    long stock,
    boolean activo
) {
}