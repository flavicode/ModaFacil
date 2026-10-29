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
    botonHeader?.addEventListener("click", () => {abrirCarrito();});

    cerrar?.addEventListener("click", cerrarCarrito);
    overlay.addEventListener("click", cerrarCarrito);

    seguirComprando?.addEventListener(
        "click", cerrarCarrito
    );

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

});
