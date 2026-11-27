package com.utp.ModaFacil.model;

import java.util.List;

public class ProveedorStock {
    private final Long idProveedor;
    private final String nombreProveedor;
    private final int subtotal;
    private final List<TallaStock> tallas;

    public ProveedorStock(Long idProveedor, String nombreProveedor, int subtotal, List<TallaStock> tallas) {
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
        this.subtotal = subtotal;
        this.tallas = tallas;
    }

    public Long getIdProveedor() { return idProveedor; }
    public String getNombreProveedor() { return nombreProveedor; }
    public int getSubtotal() { return subtotal; }
    public List<TallaStock> getTallas() { return tallas; }
}
