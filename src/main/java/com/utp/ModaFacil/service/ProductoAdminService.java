package com.utp.ModaFacil.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.dao.ProductoDAO;
import com.utp.ModaFacil.model.ProductoAdmin;
import com.utp.ModaFacil.model.ProductoDetalleAdmin;
import com.utp.ModaFacil.model.Categoria;

@Service
public class ProductoAdminService {

    private final ProductoDAO productoDAO;

    public ProductoAdminService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public List<ProductoAdmin> buscarProductos(
            String busqueda, String estado) {

        busqueda = busqueda == null
                ? ""
                : busqueda.trim();

        estado = estado == null
                ? "todos"
                : estado.trim().toLowerCase(Locale.ROOT);

        if (!List.of("todos", "activo", "inactivo")
                .contains(estado)) {
            estado = "todos";
        }

        return productoDAO.buscarProductosAdmin(busqueda, estado);
    }

    public long contarProductos() {
        return productoDAO.contarProductosAdmin();
    }

    public boolean actualizarEstado(Long idProducto, boolean activo) {
        return productoDAO.actualizarEstado(idProducto, activo);
    }

    public Optional<ProductoDetalleAdmin> obtenerDetalle(
        Long idProducto) {
            System.out.println("[SERVICE] Consultando producto: " + idProducto);//
        return productoDAO.obtenerDetalleAdmin(idProducto);
        }
    
    public List<Categoria> listarCategorias() {
        return productoDAO.listarCategorias();
    }

    public Map<String, List<Categoria>> obtenerCategoriasPorGenero() {
        return productoDAO.obtenerCategoriasPorGenero();
    }

}