document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // 1. SELECTOR DE TALLAS PRINCIPAL (ARRIBA)
    // ==========================================
    const botonesTallas = document.querySelectorAll(".comparar-talla-btn");
    const inputTalla = document.getElementById("talla-seleccionada");

    if (inputTalla && botonesTallas.length > 0) {
        function seleccionarTalla(boton) {
            if (!boton) return;

            botonesTallas.forEach(btn => {
                btn.classList.remove("activa");
                btn.setAttribute("aria-pressed", "false");
            });

            boton.classList.add("activa");
            boton.setAttribute("aria-pressed", "true");
            inputTalla.value = boton.dataset.talla;
        }

        botonesTallas.forEach(boton => {
            boton.addEventListener("click", () => seleccionarTalla(boton));
        });

        const tallaInicial = Array.from(botonesTallas).find(
            boton => boton.dataset.talla === "M"
        ) || botonesTallas[0];

        seleccionarTalla(tallaInicial);
    }

    // ==========================================
    // 2. SELECTOR DE CANTIDAD PRINCIPAL (ARRIBA)
    // ==========================================
    const btnRestar = document.getElementById("btn-restar");
    const btnSumar = document.getElementById("btn-sumar");
    const inputCantidad = document.getElementById("cantidad-producto");

    if (btnRestar && btnSumar && inputCantidad) {
        const stockMaximo = Number(inputCantidad.max);

        function actualizarBotones() {
            const cantidad = Number(inputCantidad.value);
            btnRestar.disabled = cantidad <= 1;
            btnSumar.disabled = cantidad >= stockMaximo;
        }

        btnRestar.addEventListener("click", () => {
            let cantidad = Number(inputCantidad.value);
            if (cantidad > 1) {
                inputCantidad.value = --cantidad;
            }
            actualizarBotones();
        });

        btnSumar.addEventListener("click", () => {
            let cantidad = Number(inputCantidad.value);
            if (cantidad < stockMaximo) {
                inputCantidad.value = ++cantidad;
            }
            actualizarBotones();
        });

        if (stockMaximo > 0) {
            actualizarBotones();
        } else {
            btnRestar.disabled = true;
            btnSumar.disabled = true;
        }
    }

    // ==========================================
    // 3. RADIOS DE TALLA EN LA TABLA COMPARATIVA
    // ==========================================
    document.querySelectorAll(".comparacion-form-carrito").forEach(form => {
        form.addEventListener("submit", (e) => {
            const fila = form.closest("tr");
            const radioSeleccionado = fila.querySelector(".comparacion-talla-radio:checked");

            if (!radioSeleccionado) {
                e.preventDefault();
                alert("Por favor, selecciona una talla disponible.");
                return;
            }

            const inputTallaTabla = form.querySelector(".input-talla-tabla");
            if (inputTallaTabla) {
                inputTallaTabla.value = radioSeleccionado.value;
            }
        });
    });

});
