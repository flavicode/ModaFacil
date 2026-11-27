package com.utp.ModaFacil.model;

import java.util.List;

public class ProductoInventario {
    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final String imagen;
    private final int stockTotal;
    private final List<ProveedorStock> proveedores;

    public ProductoInventario(Long id, String codigo, String nombre, String categoria, String imagen, int stockTotal, List<ProveedorStock> proveedores) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.imagen = imagen;
        this.stockTotal = stockTotal;
        this.proveedores = proveedores;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getImagen() { return imagen; }
    public int getStockTotal() { return stockTotal; }
    public List<ProveedorStock> getProveedores() { return proveedores; }
}
