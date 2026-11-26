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

// MODAL EDITAR PRODUCTO
const modalEditarElemento = document.getElementById("modalEditarProducto");
const formularioEditar = document.getElementById("formEditarProducto");
const mensajeEditar = document.getElementById("editarMensaje");
const imagenEditar = document.getElementById("editarImagen");
const archivoImagenEditar = document.getElementById("editarArchivoImagen");
const btnCambiarImagen = document.getElementById("btnCambiarImagen");
const btnGuardarProducto = document.getElementById("btnGuardarProducto");
const btnEditarInventario = document.getElementById("btnEditarInventario");

const modalEditar = bootstrap.Modal.getOrCreateInstance(modalEditarElemento, {
    backdrop: "static",
    keyboard: false
});

let peticionEditar;
let botonEditarActual;
let imagenTemporal = null;

// CARGAR INFORMACIÓN AL ABRIR EL MODAL
modalEditarElemento.addEventListener("show.bs.modal", async (event) => {

    const boton = event.relatedTarget;

    if (!boton) {
        return;
    }

    botonEditarActual = boton;

    const idProducto = boton.dataset.id;
    const url = boton.dataset.url;

    console.log("Editar producto:", idProducto);
    console.log("URL del producto:", url);

    // Cancelar una petición anterior
    if (peticionEditar) {
        peticionEditar.abort();
    }

    const controlador = new AbortController();
    peticionEditar = controlador;

    // Limpiar formulario
    formularioEditar.reset();
    imagenEditar.removeAttribute("src");
    document.getElementById("editarCodigo").textContent = "";

    // Mostrar mensaje de carga
    formularioEditar.classList.add("d-none");
    mensajeEditar.classList.remove("d-none");
    mensajeEditar.classList.remove("text-danger");
    mensajeEditar.textContent = "Cargando información del producto...";

    btnGuardarProducto.disabled = true;
    btnEditarInventario.disabled = true;

    try {
        const respuesta = await fetch(url, {
            signal: controlador.signal,
            cache: "no-store"
        });

        if (!respuesta.ok || respuesta.redirected) {
            throw new Error("No se pudo obtener el producto");
        }

        const producto = await respuesta.json();

        if (controlador.signal.aborted) {
            return;
        }

        console.log("Producto para editar:", producto);

        // Cargar información en el formulario
        mostrarEditarProducto(producto, idProducto);

        // Mostrar formulario
        mensajeEditar.classList.add("d-none");
        formularioEditar.classList.remove("d-none");

        btnEditarInventario.disabled = false;

        // El guardado se habilitará al implementar el backend
        btnGuardarProducto.disabled = true;

    } catch (error) {

        if (error.name === "AbortError") {
            return;
        }

        mensajeEditar.classList.add("text-danger");
        mensajeEditar.textContent = "No se pudo cargar la información del producto.";

        console.error(error);
    }
});

// MOSTRAR INFORMACIÓN PARA EDITAR
function mostrarEditarProducto(producto, idProducto) {

    // Función auxiliar para asignar valores
    function asignarValor(id, valor) {
        document.getElementById(id).value = valor ?? "";
    }

    // INFORMACIÓN PRINCIPAL
    asignarValor("editarIdProducto", idProducto);
    asignarValor("editarNombre", producto.nombre);

    document.getElementById("editarCodigo").textContent =
        producto.codigo ?? "";

    // CATEGORÍAS SEGÚN EL GÉNERO
    cargarCategoriasPorGenero(producto.genero,producto.idCategoria);

    // DESCRIPCIÓN
    asignarValor("editarDescripcion", producto.descripcion);

    // ESPECIFICACIONES
    asignarValor("editarMaterial", producto.material);
    asignarValor("editarTipoTela", producto.tipoTela);
    asignarValor("editarAjuste", producto.ajuste);
    asignarValor("editarManga", producto.manga);
    asignarValor("editarCuello", producto.cuello);
    asignarValor("editarGenero", producto.genero);
    asignarValor("editarOcasion", producto.ocasion);

    // IMAGEN DEL PRODUCTO
    if (producto.imagen) {

        imagenEditar.src =
            modalEditarElemento.dataset.imgBase +
            encodeURIComponent(producto.imagen);

        imagenEditar.alt = producto.nombre;
        imagenEditar.classList.remove("d-none");

    } else {

        imagenEditar.removeAttribute("src");
        imagenEditar.classList.add("d-none");
    }
}

// ABRIR EXPLORADOR DE ARCHIVOS
btnCambiarImagen.addEventListener("click", () => {
    archivoImagenEditar.click();
});

// PREVISUALIZAR NUEVA IMAGEN
archivoImagenEditar.addEventListener("change", () => {

    const archivo = archivoImagenEditar.files[0];

    if (!archivo) {
        return;
    }

    // Validar formato de imagen
    const formatosPermitidos = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ];

    if (!formatosPermitidos.includes(archivo.type)) {
        alert("Seleccione una imagen JPG, PNG o WEBP.");
        archivoImagenEditar.value = "";
        return;
    }

    // Liberar imagen temporal anterior
    if (imagenTemporal) {
        URL.revokeObjectURL(imagenTemporal);
    }

    // Crear URL temporal
    imagenTemporal = URL.createObjectURL(archivo);

    // Mostrar nueva imagen
    imagenEditar.src = imagenTemporal;
    imagenEditar.classList.remove("d-none");

    console.log("Nueva imagen seleccionada:", archivo.name);
});


// EDITAR INVENTARIO
btnEditarInventario.addEventListener("click", () => {
    const idProducto = document.getElementById("editarIdProducto").value;
    if (!idProducto) {
        return;
    }
    const url = btnEditarInventario.dataset.url;
    window.location.href = url;
});

// CANCELAR PETICIÓN AL CERRAR EL MODAL
modalEditarElemento.addEventListener("hide.bs.modal", () => {

    if (peticionEditar) {
        peticionEditar.abort();
    }
});

// LIMPIAR MODAL AL CERRAR
modalEditarElemento.addEventListener("hidden.bs.modal", () => {

    if (imagenTemporal) {
        URL.revokeObjectURL(imagenTemporal);
        imagenTemporal = null;
    }

    formularioEditar.reset();
    imagenEditar.removeAttribute("src");

    botonEditarActual?.focus();
});

// EVITAR ENVÍO DEL FORMULARIO POR AHORA
formularioEditar.addEventListener("submit", (event) => {
    event.preventDefault();
});

// CARGAR CATEGORÍAS SEGÚN EL GÉNERO
function cargarCategoriasPorGenero(genero, idCategoriaSeleccionada = null) {

    const selectCategoria = document.getElementById("editarCategoria");

    // Eliminar categorías anteriores
    selectCategoria.replaceChildren();

    // Agregar opción inicial
    selectCategoria.add(
        new Option("Seleccione una categoría", "")
    );

    // Obtener categorías correspondientes al género
    const categorias = categoriasAdminPorGenero[genero] ?? [];

    // Agregar categorías al selector
    categorias.forEach(categoria => {

        const opcion = new Option(
            categoria.nombre,
            categoria.idCategoria
        );

        selectCategoria.add(opcion);
    });

    // Seleccionar la categoría actual del producto
    if (idCategoriaSeleccionada !== null) {
        selectCategoria.value = String(idCategoriaSeleccionada);
    }

    // Mantener opción inicial si la categoría no corresponde
    if (selectCategoria.selectedIndex === -1) {
        selectCategoria.value = "";
    }

    selectCategoria.disabled = categorias.length === 0;
    // ACTUALIZAR CATEGORÍAS AL CAMBIAR EL GÉNERO
    document.getElementById("editarGenero").addEventListener("change", (event) => {

        const genero = event.target.value;

        const categoriaActual =
            document.getElementById("editarCategoria").value;

        cargarCategoriasPorGenero(genero, categoriaActual);
    });
}