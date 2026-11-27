package com.utp.ModaFacil.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.productos}")
    private String rutaImagenes;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String ubicacion = Path.of(rutaImagenes)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }

        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                    ubicacion,
                    "classpath:/static/img/"
                );
    }
}