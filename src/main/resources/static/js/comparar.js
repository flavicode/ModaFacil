
document.addEventListener("DOMContentLoaded", () => {

    // OBTENER BOTONES DE TALLAS
    const botonesTallas = document.querySelectorAll(
        ".comparar-talla-btn"
    );

    // INPUT QUE ALMACENA LA TALLA SELECCIONADA
    const inputTalla = document.getElementById(
        "talla-seleccionada"
    );

    if (!inputTalla || botonesTallas.length === 0) {
        return;
    }

    // SELECCIONAR UNA TALLA
    function seleccionarTalla(boton) {

        // QUITAR SELECCIÓN ANTERIOR
        botonesTallas.forEach(btn => {
            btn.classList.remove("activa");
            btn.setAttribute("aria-pressed", "false");
        });

        // ACTIVAR NUEVA TALLA
        boton.classList.add("activa");
        boton.setAttribute("aria-pressed", "true");

        // GUARDAR VALOR
        inputTalla.value = boton.dataset.talla;

        console.log("Talla seleccionada:", inputTalla.value);
    }

    // REGISTRAR EVENTOS
    botonesTallas.forEach(boton => {

        boton.addEventListener("click", () => {
            seleccionarTalla(boton);
        });

    });

    // TALLA PREDETERMINADA: M SI EXISTE
    const tallaInicial = Array.from(botonesTallas).find(
        boton => boton.dataset.talla === "M"
    ) || botonesTallas[0];

    seleccionarTalla(tallaInicial);

});



document.addEventListener("DOMContentLoaded", () => {

    // ELEMENTOS DEL SELECTOR
    const btnRestar = document.getElementById("btn-restar");
    const btnSumar = document.getElementById("btn-sumar");
    const inputCantidad = document.getElementById(
        "cantidad-producto"
    );

    // VERIFICAR QUE EXISTAN
    if (!btnRestar || !btnSumar || !inputCantidad) {
        return;
    }

    // STOCK DISPONIBLE
    const stockMaximo = Number(inputCantidad.max);

    // ACTUALIZAR ESTADO DE BOTONES
    function actualizarBotones() {

        const cantidad = Number(inputCantidad.value);

        btnRestar.disabled = cantidad <= 1;
        btnSumar.disabled = cantidad >= stockMaximo;
    }

    // DISMINUIR CANTIDAD
    btnRestar.addEventListener("click", () => {

        let cantidad = Number(inputCantidad.value);

        if (cantidad > 1) {
            cantidad--;
            inputCantidad.value = cantidad;
        }

        actualizarBotones();
    });

    // AUMENTAR CANTIDAD
    btnSumar.addEventListener("click", () => {

        let cantidad = Number(inputCantidad.value);

        if (cantidad < stockMaximo) {
            cantidad++;
            inputCantidad.value = cantidad;
        }

        actualizarBotones();
    });

    // ESTADO INICIAL
    if (stockMaximo > 0) {
        actualizarBotones();
    } else {
        btnRestar.disabled = true;
        btnSumar.disabled = true;
    }

});