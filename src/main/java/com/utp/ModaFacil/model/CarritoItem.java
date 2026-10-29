package com.utp.ModaFacil.model;

import java.math.BigDecimal;

public class CarritoItem {

    private Long idProducto;
    private Long idImportador;
    private String nombre;
    private String imagen;
    private String talla;
    private String importador;
    private BigDecimal precioUnitario;
    private int cantidad;

    public CarritoItem(Long idProducto,
                       Long idImportador,
                       String nombre,
                       String imagen,
                       String talla,
                       String importador,
                       BigDecimal precioUnitario,
                       int cantidad) {

        this.idProducto = idProducto;
        this.idImportador = idImportador;
        this.nombre = nombre;
        this.imagen = imagen;
        this.talla = talla;
        this.importador = importador;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public Long getIdImportador() {
        return idImportador;
    }

    public String getNombre() {
        return nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public String getTalla() {
        return talla;
    }

    public String getImportador() {
        return importador;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}

