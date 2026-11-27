document.addEventListener("DOMContentLoaded", () => {

    const carrito = document.getElementById("mini-carrito");
    const overlay = document.getElementById("carrito-overlay");
    const cerrar = document.getElementById("cerrar-carrito");
    const seguirComprando = document.getElementById("seguir-comprando");
    const botonHeader = document.getElementById("btn-abrir-carrito");

    if (!carrito || !overlay) return;

    function abrirCarrito() {
        carrito.classList.add("activo");
        overlay.classList.add("activo");
        carrito.setAttribute("aria-hidden", "false");
    }

    function cerrarCarrito() {
        carrito.classList.remove("activo");
        overlay.classList.remove("activo");
        carrito.setAttribute("aria-hidden", "true");
    }

    // ABRIR EL CARRITO DESDE EL HEADER
    botonHeader?.addEventListener("click", () => { abrirCarrito(); });

    cerrar?.addEventListener("click", cerrarCarrito);
    overlay.addEventListener("click", cerrarCarrito);

    seguirComprando?.addEventListener("click", cerrarCarrito);

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            cerrarCarrito();
        }
    });

    // ABRIR DESPUÉS DE AGREGAR UNA PRENDA
    const parametros = new URLSearchParams(window.location.search);

    if (parametros.get("carrito") === "abierto") {
        abrirCarrito();
    }

    // ==========================================
    // ACTUALIZAR CANTIDADES SIN RECARGAR (AJAX)
    // ==========================================
    document.querySelectorAll(".mini-carrito-cantidad form, .mini-carrito-eliminar-form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault(); // Evita que la página se recargue

            const formData = new FormData(form);
            const urlAjax = form.getAttribute("action") + "/ajax";

            const resp = await fetch(urlAjax, {
                method: "POST",
                body: formData
            });

            if (!resp.ok) return;
            const data = await resp.json();
            if (!data.ok) return;

            const tarjetaItem = form.closest(".mini-carrito-item");

            if (data.eliminado) {
                tarjetaItem.remove();
            } else {
                tarjetaItem.querySelector(".mini-carrito-cantidad span").textContent = data.cantidad;
                tarjetaItem.querySelector(".mini-carrito-item-subtotal span").textContent = data.subtotalItem.replace(".", ",");
                const inputEliminar = tarjetaItem.querySelector(".mini-carrito-eliminar-form input[name='cambio']");
                if (inputEliminar) inputEliminar.value = -data.cantidad;
            }

            // Actualizar subtotal general y contadores
            const subtotalSpan = document.querySelector(".mini-carrito-subtotal span");
            if (subtotalSpan) subtotalSpan.textContent = data.subtotalCarrito.replace(".", ",");

            const tituloContador = document.querySelector("#titulo-carrito span");
            if (tituloContador) tituloContador.textContent = data.totalItems;

            const badgeHeader = document.querySelector(".carrito-contador");
            if (badgeHeader) badgeHeader.textContent = data.totalItems;

            if (data.totalItems === 0) {
                document.querySelector(".mini-carrito-productos").innerHTML = "<p>Tu carrito está vacío.</p>";
            }
        });
    });

});
