document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // 1. SELECTOR DE TALLAS PRINCIPAL (ARRIBA)
    // ==========================================
    const botonesTallas = document.querySelectorAll(".comparar-talla-btn");
    const inputTalla = document.getElementById("talla-seleccionada");

    // Obtener todas las tallas que tienen stock en al menos un importador de la tabla
    const radiosConStock = document.querySelectorAll(".comparacion-talla-radio:not(:disabled)");
    const tallasDisponiblesGlobal = new Set(
        Array.from(radiosConStock).map(radio => radio.value)
    );

    if (inputTalla && botonesTallas.length > 0) {
        // Deshabilitar arriba las tallas que no tienen stock en ningún importador
        botonesTallas.forEach(boton => {
            const tieneStock = tallasDisponiblesGlobal.has(boton.dataset.talla);
            boton.disabled = !tieneStock;
            if (!tieneStock) {
                boton.classList.add("talla-sin-stock");
            }
        });

        function seleccionarTalla(boton) {
            if (!boton || boton.disabled) return;

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

        // Seleccionar predefinido el PRIMER botón habilitado (con stock)
        const tallaInicial = Array.from(botonesTallas).find(boton => !boton.disabled);
        if (tallaInicial) {
            seleccionarTalla(tallaInicial);
        }
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
    document.querySelectorAll(".comparacion-tabla tbody tr").forEach(fila => {
        const radios = fila.querySelectorAll(".comparacion-talla-radio:not(:disabled)");
        const form = fila.querySelector(".comparacion-form-carrito");
        const inputTallaTabla = form ? form.querySelector(".input-talla-tabla") : null;

        if (radios.length > 0) {
            let radioActivo = Array.from(radios).find(r => r.checked);
            if (!radioActivo) {
                radios[0].checked = true;
                radioActivo = radios[0];
            }
            if (inputTallaTabla) {
                inputTallaTabla.value = radioActivo.value;
            }
        }

        radios.forEach(radio => {
            radio.addEventListener("change", () => {
                if (radio.checked && inputTallaTabla) {
                    inputTallaTabla.value = radio.value;
                }
            });
        });

        if (form) {
            form.addEventListener("submit", (e) => {
                const radioSeleccionado = fila.querySelector(".comparacion-talla-radio:checked");

                if (!radioSeleccionado) {
                    e.preventDefault();
                    alert("Por favor, selecciona una talla disponible.");
                    return;
                }

                if (inputTallaTabla) {
                    inputTallaTabla.value = radioSeleccionado.value;
                }
            });
        }
    });

});
