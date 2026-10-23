package com.utp.ModaFacil.model;

public class Categoria {
    private Long idCategoria;
    private String nombre;
    private String color;

    public Categoria() {}

    public Categoria(Long idCategoria, String nombre, String color) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.color = color;
    }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
