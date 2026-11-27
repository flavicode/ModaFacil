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
import com.utp.ModaFacil.model.ProductoEditarAdmin;

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
    
    public boolean actualizarProducto(
        Long idProducto,
        ProductoEditarAdmin producto,
        String imagen) {
        // Validar nombre
        if (producto.nombre() == null || producto.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        // Validar categoría
        if (producto.idCategoria() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        // Validar género
        if (producto.genero() == null ||
            !List.of("Hombre", "Mujer").contains(producto.genero())) {
            throw new IllegalArgumentException("El género seleccionado no es válido.");
        }
        // Validar relación entre categoría y género
        if (!productoDAO.existeCategoriaPorGenero(
                producto.idCategoria(), producto.genero())) {
            throw new IllegalArgumentException(
                "La categoría no corresponde al género seleccionado."
            );
        }
        // Validar longitud de los campos
        validarLongitud(producto.nombre(), 120, "Nombre");
        validarLongitud(producto.descripcion(), 255, "Descripción");
        validarLongitud(producto.material(), 60, "Material");
        validarLongitud(producto.tipoTela(), 60, "Tipo de tela");
        validarLongitud(producto.ajuste(), 40, "Ajuste");
        validarLongitud(producto.manga(), 40, "Manga");
        validarLongitud(producto.cuello(), 60, "Cuello");
        validarLongitud(producto.ocasion(), 60, "Ocasión");
        // Actualizar producto
        return productoDAO.actualizarProductoAdmin(idProducto,producto,imagen
        );
    }

    private void validarLongitud(String valor, int maximo, String campo) {
    if (valor != null && valor.length() > maximo) {
        throw new IllegalArgumentException(
            campo + " no puede superar los " + maximo + " caracteres."
        );
    }
}

}