package com.utp.ModaFacil.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenProductoService {

    private final Path directorio;

    public ImagenProductoService(
            @Value("${app.upload.productos}") String ruta) {
        this.directorio = Path.of(ruta)
                .toAbsolutePath()
                .normalize();
    }

    // GUARDAR IMAGEN
    public String guardarImagen(MultipartFile imagen) throws IOException {

        if (imagen == null || imagen.isEmpty()) {
            return null;
        }

        // Validar tamaño máximo: 5 MB
        if (imagen.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "La imagen no puede superar los 5 MB."
            );
        }

        // Obtener contenido del archivo
        byte[] contenido = imagen.getBytes();

        // Identificar formato
        String extension = obtenerExtension(contenido);

        // Crear directorio si no existe
        Files.createDirectories(directorio);

        // Generar nombre único
        String nombreArchivo =
            "producto-" + UUID.randomUUID() + extension;

        // Obtener ruta de almacenamiento
        Path destino = directorio.resolve(nombreArchivo);

        // Guardar imagen
        Files.write(destino, contenido, StandardOpenOption.CREATE_NEW);
        // Mostrar ubicación del archivo
        System.out.println("DIRECTORIO DE IMÁGENES: " + directorio);
        System.out.println("IMAGEN GUARDADA EN: " + destino);
        return nombreArchivo;
    }

    // VALIDAR FIRMA DEL ARCHIVO
    private String obtenerExtension(byte[] datos) {

        // JPG / JPEG
        if (datos.length >= 3 &&
            (datos[0] & 0xFF) == 0xFF &&
            (datos[1] & 0xFF) == 0xD8 &&
            (datos[2] & 0xFF) == 0xFF) {

            return ".jpg";
        }

        // PNG
        if (datos.length >= 8 &&
            (datos[0] & 0xFF) == 0x89 &&
            datos[1] == 'P' &&
            datos[2] == 'N' &&
            datos[3] == 'G' &&
            datos[4] == 13 &&
            datos[5] == 10 &&
            datos[6] == 26 &&
            datos[7] == 10) {

            return ".png";
        }

        // WEBP
        if (datos.length >= 12 &&
            datos[0] == 'R' &&
            datos[1] == 'I' &&
            datos[2] == 'F' &&
            datos[3] == 'F' &&
            datos[8] == 'W' &&
            datos[9] == 'E' &&
            datos[10] == 'B' &&
            datos[11] == 'P') {

            return ".webp";
        }

        throw new IllegalArgumentException(
            "Seleccione una imagen JPG, PNG o WEBP válida."
        );
    }

    // ELIMINAR IMAGEN NUEVA SI FALLA LA ACTUALIZACIÓN
    public void eliminarImagen(String nombreArchivo) {

        if (nombreArchivo == null) {
            return;
        }

        try {
            Path archivo = directorio.resolve(nombreArchivo).normalize();

            if (directorio.equals(archivo.getParent())) {
                Files.deleteIfExists(archivo);
            }

        } catch (IOException e) {
            System.err.println(
                "No se pudo eliminar la imagen: " + nombreArchivo
            );
        }
    }
}