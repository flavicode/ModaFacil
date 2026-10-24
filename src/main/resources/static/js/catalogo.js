document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // 1. CATEGORÍAS POR GÉNERO
    // ==========================================

    const categoriasPorGenero = {
        Hombre: [
            "Polos", "Pantalones", "Camisas",
            "Casacas", "Shorts", "Jeans",
            "Poleras", "Ropa deportiva"
        ],
        Mujer: [
            "Vestidos", "Blusas", "Pantalones",
            "Faldas", "Chaquetas", "Polos",
            "Jeans", "Tops", "Ropa deportiva"
        ]
    };

    // ==========================================
    // 2. ESTADO INICIAL
    // ==========================================

    let generoSeleccionado = "Todos";
    let categoriaSeleccionada = "Todas";

    // ==========================================
    // 3. ELEMENTOS HTML
    // ==========================================

    const contenedorCategorias =
        document.getElementById("contenedor-categorias");

    const viewport =
        document.getElementById("categorias-viewport");

    const botonMas =
        document.getElementById("btn-categorias-mas");

    const botonAnterior =
        document.getElementById("btn-categorias-anterior");

    const botonesGenero =
        document.querySelectorAll(".genero-btn");

    const botonRestablecer =
        document.getElementById("ver-todos");

    // ==========================================
    // 4. NOTIFICAR CAMBIOS DE FILTROS
    // ==========================================

    function notificarFiltros() {

        document.dispatchEvent(
            new CustomEvent("catalogo:filtros-cambiados", {
                detail: {
                    genero: generoSeleccionado,
                    categoria: categoriaSeleccionada
                }
            })
        );
    }

    // ==========================================
    // 5. OBTENER CATEGORÍAS
    // ==========================================

    function obtenerCategorias() {

        if (generoSeleccionado === "Todos") {

            return [...new Set([
                ...categoriasPorGenero.Hombre,
                ...categoriasPorGenero.Mujer
            ])];
        }

        return categoriasPorGenero[generoSeleccionado];
    }

    // ==========================================
    // 6. CREAR BOTÓN DE CATEGORÍA
    // ==========================================

    function crearBotonCategoria(categoria) {

        const boton = document.createElement("button");

        boton.type = "button";
        boton.className = "btn categoria-btn";
        boton.textContent = categoria;
        boton.dataset.categoria = categoria;

        if (categoria === categoriaSeleccionada) {
            boton.classList.add("active");
        }

        boton.addEventListener("click", () => {

            categoriaSeleccionada = categoria;

            contenedorCategorias
                .querySelectorAll(".categoria-btn")
                .forEach(b => b.classList.remove("active"));

            boton.classList.add("active");

            // Notificar a producto.js
            notificarFiltros();
        });

        return boton;
    }

    // ==========================================
    // 7. RENDERIZAR CATEGORÍAS
    // ==========================================

    function renderizarCategorias() {

        contenedorCategorias.replaceChildren();

        const categorias = obtenerCategorias();

        categorias.forEach(categoria => {

            contenedorCategorias.appendChild(
                crearBotonCategoria(categoria)
            );
        });

        // Regresar al inicio del carrusel
        viewport.scrollLeft = 0;

        actualizarNavegacion();
    }

    // ==========================================
    // 8. ACTUALIZAR NAVEGACIÓN
    // ==========================================

    function actualizarNavegacion() {

        botonAnterior.hidden =
            viewport.scrollLeft <= 1;

        // + Más siempre permanece habilitado
        botonMas.disabled = false;
    }

    // ==========================================
    // 9. BOTÓN + MÁS
    // ==========================================

    botonMas.addEventListener("click", () => {

        const maximo =
            viewport.scrollWidth - viewport.clientWidth;

        if (viewport.scrollLeft >= maximo - 1) {

            viewport.scrollTo({
                left: 0,
                behavior: "smooth"
            });

        } else {

            viewport.scrollBy({
                left: viewport.clientWidth * 0.75,
                behavior: "smooth"
            });
        }
    });

    // ==========================================
    // 10. BOTÓN ANTERIOR
    // ==========================================

    botonAnterior.addEventListener("click", () => {

        viewport.scrollBy({
            left: -viewport.clientWidth * 0.75,
            behavior: "smooth"
        });
    });

    // ==========================================
    // 11. EVENTOS DE DESPLAZAMIENTO
    // ==========================================

    viewport.addEventListener(
        "scroll",
        actualizarNavegacion
    );

    window.addEventListener(
        "resize",
        actualizarNavegacion
    );

    // ==========================================
    // 12. SELECCIONAR HOMBRE O MUJER
    // ==========================================

    botonesGenero.forEach(boton => {

        boton.addEventListener("click", () => {

            generoSeleccionado = boton.dataset.genero;
            categoriaSeleccionada = "Todas";

            botonesGenero.forEach(b => {
                b.classList.toggle("active", b === boton);
            });

            renderizarCategorias();

            // Actualizar los productos
            notificarFiltros();
        });
    });

    // ==========================================
    // 13. BOTÓN VER TODOS
    // ==========================================

    botonRestablecer.addEventListener("click", () => {

        generoSeleccionado = "Todos";
        categoriaSeleccionada = "Todas";

        botonesGenero.forEach(b => {
            b.classList.remove("active");
        });

        renderizarCategorias();

        // Mostrar productos de ambos géneros
        notificarFiltros();
    });

    // ==========================================
    // 14. INICIALIZACIÓN
    // ==========================================

    renderizarCategorias();

});