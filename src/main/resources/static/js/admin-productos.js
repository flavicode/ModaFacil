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