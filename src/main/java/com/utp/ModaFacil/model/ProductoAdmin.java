package com.utp.ModaFacil.model;

public record ProductoAdmin(
    Long idProducto,
    String codigo,
    String nombre,
    String imagen,
    String categoria,
    boolean activo
) {
}