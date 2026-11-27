package com.utp.ModaFacil.model;

import java.math.BigDecimal;

public class CarritoItem {

    private Long idProducto;
    private Long idProveedor;
    private String nombre;
    private String imagen;
    private String talla;
    private String proveedor;
    private BigDecimal precioUnitario;
    private int cantidad;

    public CarritoItem(Long idProducto,
                       Long idProveedor,
                       String nombre,
                       String imagen,
                       String talla,
                       String proveedor,
                       BigDecimal precioUnitario,
                       int cantidad) {

        this.idProducto = idProducto;
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.imagen = imagen;
        this.talla = talla;
        this.proveedor = proveedor;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public Long getIdProveedor() {
        return idProveedor;
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

    public String getProveedor() {
        return proveedor;
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

