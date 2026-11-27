package com.utp.ModaFacil.model;

public class Proveedor {
    private Long idProveedor;
    private String nombre;
    private String ruc;
    private String contacto;
    private String telefono;
    private int productosAsociados;

    public Proveedor() {
    }

    public Proveedor(Long idProveedor, String nombre, String ruc, String contacto, String telefono, boolean activo, int productosAsociados) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.ruc = ruc;
        this.contacto = contacto;
        this.telefono = telefono;
        this.productosAsociados = productosAsociados;
    }

    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getProductosAsociados() {
        return productosAsociados;
    }

    public void setProductosAsociados(int productosAsociados) {
        this.productosAsociados = productosAsociados;
    }
}