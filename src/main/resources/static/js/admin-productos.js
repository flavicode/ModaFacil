const formulario = document.getElementById("formFiltros");
const buscarProducto = document.getElementById("buscarProducto");
const filtroEstado = document.getElementById("filtroEstado");

let temporizador;

// Buscar producto
buscarProducto.addEventListener("input", () => {

    clearTimeout(temporizador);

    temporizador = setTimeout(() => {
        formulario.requestSubmit();
    }, 600);

});

// Filtrar por estado
filtroEstado.addEventListener("change", () => {

    clearTimeout(temporizador);

    formulario.requestSubmit();

});

// ACTIVAR Y DESACTIVAR PRODUCTOS
document.addEventListener("change", async (event) => {

    // Verificar que sea un interruptor de estado
    if (!event.target.matches(".switch-estado")) {
        return;
    }

    const interruptor = event.target;
    const idProducto = interruptor.dataset.id;
    const url = interruptor.dataset.url;
    const activo = interruptor.checked;

    // Obtener la fila correspondiente
    const fila = interruptor.closest("tr");
    // Obtener la etiqueta de estado
    const etiqueta = fila.querySelector(".estado-producto");
    // Deshabilitar mientras se realiza la petición
    interruptor.disabled = true;

    try {
        const respuesta = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                activo: activo
            })
        });

        if (!respuesta.ok) {
            throw new Error("No se pudo actualizar el estado");
        }

        const resultado = await respuesta.json();

        if (!resultado.ok) {
            throw new Error(resultado.mensaje);
        }

        // Actualizar el interruptor
        interruptor.checked = resultado.activo;
        // Actualizar el texto
        etiqueta.textContent =
            resultado.activo ? "Activo" : "Inactivo";
        // Actualizar los colores
        etiqueta.classList.toggle(
            "estado-activo",
            resultado.activo
        );
        etiqueta.classList.toggle(
            "estado-inactivo",
            !resultado.activo
        );
        console.log(
            "Producto actualizado:",
            idProducto,
            resultado.activo
        );

        // Actualizar resultados si hay un filtro de estado
        const estadoFiltro =
            document.getElementById("filtroEstado");
        if (estadoFiltro && estadoFiltro.value !== "todos") {
            document.getElementById("formFiltros")
                    .requestSubmit();
        }

    } catch (error) {
        // Restaurar el estado anterior del interruptor
        interruptor.checked = !activo;
        alert("No se pudo actualizar el estado del producto.");
        console.error(error);

    } finally {
        interruptor.disabled = false;

    }
});

// ACCIÓN EDITAR PRODUCTO
document.addEventListener("click", (event) => {

    const btnEditar = event.target.closest(".btn-editar");

    if (btnEditar) {
        const idProducto = btnEditar.dataset.id;
        console.log("Editar producto:", idProducto);
    }

});


// MODAL DETALLE DEL PRODUCTO
// Obtener el elemento HTML del modal
const modalDetalleElemento = document.getElementById(
    "modalDetalleProducto"
);

// Crear instancia del modal de Bootstrap
const modalDetalle = new bootstrap.Modal(
    modalDetalleElemento,
    {
        backdrop: "static",
        keyboard: false
    }
);

let peticionDetalle;
let botonDetalleActual;

// Detectar clic en el botón del ojo
document.addEventListener("click", async (event) => {
    const boton = event.target.closest(".btn-detalle");
    if (!boton) {
        return;
    }
    botonDetalleActual = boton;
    const url = boton.dataset.url;
    //
    console.log("ID del producto:", boton.dataset.id);
    console.log("URL del detalle:", url);
    // Cancelar una petición anterior
    if (peticionDetalle) {
        peticionDetalle.abort();
    }
    const controlador = new AbortController();
    peticionDetalle = controlador;
    const mensaje = document.getElementById("detalleMensaje");
    const contenido = document.getElementById("detalleContenido");
    // Mostrar mensaje de carga
    contenido.classList.add("d-none");
    mensaje.classList.remove("d-none");
    mensaje.textContent = "Cargando detalle del producto...";
    // Abrir modal
    modalDetalle.show(boton);
    try {
        // Consultar al Controller
        const respuesta = await fetch(url, {
            signal: controlador.signal,
            cache: "no-store"
        });
        //
        console.log("Estado HTTP:", respuesta.status);
        console.log("Respuesta:", respuesta);
        if (!respuesta.ok || respuesta.redirected) {
            throw new Error("No se pudo obtener el producto");
        }
        // Convertir la respuesta a JSON
        const producto = await respuesta.json();
        //
        console.log("Producto recibido:", producto);
        if (controlador.signal.aborted) {
            return;
        }
        // Mostrar información
        mostrarDetalleProducto(producto);
        // Ocultar mensaje de carga
        mensaje.classList.add("d-none");
        // Mostrar contenido
        contenido.classList.remove("d-none");
    } catch (error) {
        if (error.name === "AbortError") {
            return;
        }
        mensaje.textContent =
            "No se pudo cargar el detalle del producto.";
        console.error(error);
    }
});
// Cancelar petición al cerrar el modal
modalDetalleElemento.addEventListener("hide.bs.modal", () => {
    if (peticionDetalle) {
        peticionDetalle.abort();
    }
});
// Devolver el foco al botón que abrió el modal
modalDetalleElemento.addEventListener("hidden.bs.modal", () => {
    botonDetalleActual?.focus();
});



// MOSTRAR INFORMACIÓN DEL PRODUCTO
function mostrarDetalleProducto(producto) {
    // Función auxiliar para mostrar textos
    function asignarTexto(id, valor) {
        document.getElementById(id).textContent =
            valor ?? "No especificado";
    }
    // INFORMACIÓN PRINCIPAL
    asignarTexto("detalleNombre", producto.nombre);
    asignarTexto(
        "detalleCodigo",
        "Código: " + producto.codigo
    );
    asignarTexto(
        "detalleCategoria",
        producto.categoria
    );
    // IMAGEN
    const imagen = document.getElementById("detalleImagen");
    if (producto.imagen) {
        imagen.src =
            modalDetalleElemento.dataset.imgBase +
            encodeURIComponent(producto.imagen);
        imagen.alt = producto.nombre;
        imagen.classList.remove("d-none");
    } else {
        imagen.removeAttribute("src");
        imagen.classList.add("d-none");
    }

    // DESCRIPCIÓN
    asignarTexto(
        "detalleDescripcion",
        producto.descripcion
    );
    // ESPECIFICACIONES
    asignarTexto("detalleMaterial", producto.material);
    asignarTexto("detalleTipoTela", producto.tipoTela);
    asignarTexto("detalleAjuste", producto.ajuste);
    asignarTexto("detalleManga", producto.manga);
    asignarTexto("detalleCuello", producto.cuello);
    asignarTexto("detalleGenero", producto.genero);
    asignarTexto("detalleOcasion", producto.ocasion);

    // ESTADO
    const estado = document.getElementById("detalleEstado");
    estado.textContent = producto.activo
        ? "Activo"
        : "Inactivo";
    estado.classList.toggle(
        "estado-activo",
        producto.activo
    );
    estado.classList.toggle(
        "estado-inactivo",
        !producto.activo
    );

    // TALLAS
    const contenedorTallas =
        document.getElementById("detalleTallas");

    // Limpiar las tallas anteriores
    contenedorTallas.replaceChildren();
    if (producto.tallas && producto.tallas.length > 0) {
        producto.tallas.forEach((talla) => {
            const elemento = document.createElement("span");
            elemento.className = "detalle-talla";
            elemento.textContent = talla;
            contenedorTallas.appendChild(elemento);
        });
    } else {
        contenedorTallas.textContent =
            "Sin tallas registradas";
    }
}