package com.utp.ModaFacil.model;

public class TallaStock {
    private final String talla;
    private final int stock;

    public TallaStock(String talla, int stock) {
        this.talla = talla;
        this.stock = stock;
    }

    public String getTalla() { return talla; }
    public int getStock() { return stock; }
}
