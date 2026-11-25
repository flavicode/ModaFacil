package com.utp.ModaFacil.model;

import java.util.List;

public record ProductoDetalleAdmin(
    Long idProducto,
    String codigo,
    String nombre,
    String imagen,
    String categoria,
    boolean activo,
    String descripcion,
    String material,
    String tipoTela,
    String ajuste,
    String manga,
    String cuello,
    String genero,
    String ocasion,
    List<String> tallas
) {
}