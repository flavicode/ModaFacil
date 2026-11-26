package com.utp.ModaFacil.model;

public record ProductoEditarAdmin(
    String nombre,
    Long idCategoria,
    String descripcion,
    String material,
    String tipoTela,
    String ajuste,
    String manga,
    String cuello,
    String genero,
    String ocasion
) {
}