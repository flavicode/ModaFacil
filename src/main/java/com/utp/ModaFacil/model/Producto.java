package com.utp.ModaFacil.model;

public class Producto {
    private Long idProducto;
    private Categoria categoria;
    private String nombre;
    private String imagen;

    public Producto() {}

    public Producto(Long idProducto, Categoria categoria, String nombre, String imagen) {
        this.idProducto = idProducto;
        this.categoria = categoria;
        this.nombre = nombre;
        this.imagen = imagen;
    }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
