package com.utp.ModaFacil.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.utp.ModaFacil.dao.ProveedorDAO;
import com.utp.ModaFacil.model.Proveedor;

@Service
public class ProveedorService {

    private final ProveedorDAO proveedorDAO;

    public ProveedorService(ProveedorDAO proveedorDAO) {
        this.proveedorDAO = proveedorDAO;
    }

    public List<Proveedor> listarProveedores(String busqueda, String estado) {
        return proveedorDAO.listarProveedores(busqueda, estado);
    }

    public Optional<Proveedor> obtenerPorId(Long idProveedor) {
        return proveedorDAO.buscarPorId(idProveedor);
    }

    public boolean registrarProveedor(Proveedor proveedor) {
        validarProveedor(proveedor);
        return proveedorDAO.registrar(proveedor) > 0;
    }

    public boolean actualizarProveedor(Long idProveedor, Proveedor datos) {
        validarProveedor(datos);
        datos.setIdProveedor(idProveedor);
        return proveedorDAO.actualizar(datos) > 0;
    }

    public boolean cambiarEstado(Long idProveedor, boolean activo) {
        return proveedorDAO.actualizarEstado(idProveedor, activo) > 0;
    }

    public boolean eliminarProveedor(Long idProveedor) {
        return proveedorDAO.eliminar(idProveedor) > 0;
    }

    private void validarProveedor(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }
        if (proveedor.getRuc() != null && !proveedor.getRuc().isBlank()) {
            String rucLimpio = proveedor.getRuc().trim();
            if (!rucLimpio.matches("\\d{11}")) {
                throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos.");
            }
            proveedor.setRuc(rucLimpio);
        } else {
            proveedor.setRuc(null);
        }
        proveedor.setNombre(proveedor.getNombre().trim());
        proveedor.setContacto(proveedor.getContacto() != null && !proveedor.getContacto().isBlank()
                ? proveedor.getContacto().trim() : null);
        proveedor.setTelefono(proveedor.getTelefono() != null && !proveedor.getTelefono().isBlank()
                ? proveedor.getTelefono().trim() : null);
    }
}