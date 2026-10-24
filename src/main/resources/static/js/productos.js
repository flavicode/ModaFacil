
document.addEventListener("DOMContentLoaded", () => {

    const contenedor = document.getElementById("contenedor-productos");
    const buscador = document.getElementById("input-buscador");
    const contador = document.getElementById("contador-productos-texto");
    const categoriaTexto = document.getElementById("categoria-actual-texto");
    const sinResultados = document.getElementById("sin-resultados");
    const botonVerTodo = document.getElementById("btn-ver-todo");

    let filtrosActuales = {
    genero: "Todos",
    categoria: "Todas"
    };

    let mostrarTodos = false;
    const productos = Array.isArray(window.productosCatalogo)
    ? window.productosCatalogo
    : [];

    console.log("Productos recibidos:", productos.length);

    if (productos.length === 0) {
        console.warn(
            "No se recibieron productos desde productos-data.js"
        );
    }

    // 3. FUNCIONES AUXILIARES

    function normalizar(texto) {
        return String(texto || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .trim();
    }

    function dinero(valor) {
        return "S/ " + valor.toFixed(2);
    }

    function obtenerPrecio(producto) {

        if (producto.precios.length === 0) {
            return "Precio no disponible";
        }

        const precioMinimo = Math.min(...producto.precios);

        return "S/ " + precioMinimo.toFixed(2);
    }

    function obtenerImagen(ruta) {
        if (!ruta) return "";

        if (/^https?:\/\//i.test(ruta)) {
            return ruta;
        }

        if (ruta.startsWith("/")) {
            return ruta;
        }

        return "/img/" + ruta;
    }

    // 4. CREAR UNA TARJETA

    function crearTarjeta(producto) {

        const columna = document.createElement("div");
        columna.className = "col";

        const card = document.createElement("article");
        card.className = "card producto-card shadow-sm";

        const imagenWrapper = document.createElement("div");
        imagenWrapper.className = "producto-imagen-wrapper";

        const imagen = document.createElement("img");
        imagen.className = "producto-imagen";
        imagen.alt = producto.nombre;
        imagen.loading = "lazy";

        const placeholder = document.createElement("div");
        placeholder.className =
            "h-100 d-flex align-items-center justify-content-center text-secondary";

        const icono = document.createElement("i");
        icono.className = "bi bi-image fs-1";
        placeholder.appendChild(icono);

        const rutaImagen = obtenerImagen(producto.imagen);

        if (rutaImagen) {
            imagen.src = rutaImagen;
            imagen.onerror = () => {
                imagen.remove();
                imagenWrapper.prepend(placeholder);
            };
            imagenWrapper.appendChild(imagen);
        } else {
            imagenWrapper.appendChild(placeholder);
        }

        const favorito = document.createElement("button");
        favorito.type = "button";
        favorito.className = "producto-favorito";
        favorito.setAttribute("aria-label", "Marcar favorito");
        favorito.innerHTML = '<i class="bi bi-heart"></i>';

        favorito.addEventListener("click", () => {
            favorito.classList.toggle("activo");

            const iconoFavorito = favorito.querySelector("i");
            iconoFavorito.className =
                favorito.classList.contains("activo")
                    ? "bi bi-heart-fill"
                    : "bi bi-heart";
        });

        imagenWrapper.appendChild(favorito);

        const body = document.createElement("div");
        body.className = "card-body producto-body d-flex flex-column";

        const nombre = document.createElement("h5");
        nombre.className = "producto-nombre";
        nombre.textContent = producto.nombre;

        const precio = document.createElement("p");
        precio.className = "producto-precio";
        precio.textContent = obtenerPrecio(producto);

        const pie = document.createElement("div");
        pie.className =
            "d-flex justify-content-between align-items-center gap-2 mt-auto";

        const proveedores = document.createElement("span");
        proveedores.className = "producto-proveedores";

        const cantidad = producto.proveedores.size;

        proveedores.textContent =
            cantidad > 5
                ? "5+ proveedores"
                : cantidad + (cantidad === 1 ? " proveedor" : " proveedores");

        const comparar = document.createElement("a");
        comparar.className = "btn btn-comparar";
        comparar.textContent = "Comparar Precios";
        comparar.href = "/comparar?id=" + encodeURIComponent(producto.id);

        pie.append(proveedores, comparar);
        body.append(nombre, precio, pie);
        card.append(imagenWrapper, body);
        columna.appendChild(card);

        return columna;
    }

    // 5. FILTRAR Y RENDERIZAR

    function renderizarProductos() {

        const texto = normalizar(buscador.value);

        const filtrados = productos.filter(producto => {

            // FILTRO POR BÚSQUEDA
            const coincideBusqueda =
                normalizar(producto.nombre).includes(texto) ||
                normalizar(producto.categoria).includes(texto) ||
                normalizar(producto.descripcion).includes(texto);

            // FILTRO POR GÉNERO
            const coincideGenero =
                filtrosActuales.genero === "Todos" ||
                normalizar(producto.genero) ===
                normalizar(filtrosActuales.genero);

            // FILTRO POR CATEGORÍA
            const coincideCategoria =
                filtrosActuales.categoria === "Todas" ||
                normalizar(producto.categoria) ===
                normalizar(filtrosActuales.categoria);

            return coincideBusqueda &&
                coincideGenero &&
                coincideCategoria;
        });

        const visibles = mostrarTodos
            ? filtrados
            : filtrados.slice(0, 6);

        contenedor.replaceChildren();

        visibles.forEach(producto => {
            contenedor.appendChild(crearTarjeta(producto));
        });

        if (contador) {
            contador.textContent = filtrados.length + " productos";
        }

        if (categoriaTexto) {
            categoriaTexto.textContent = filtrosActuales.categoria;
        }

        sinResultados.classList.toggle(
            "d-none",
            filtrados.length > 0
        );

        botonVerTodo.classList.toggle(
            "d-none",
            filtrados.length <= 6
        );

        botonVerTodo.textContent = mostrarTodos
            ? "Mostrar Menos"
            : "Ver Todo el Catálogo";
    }

    // 6. BUSCADOR

    buscador.addEventListener("input", () => {
        mostrarTodos = false;
        renderizarProductos();
    });

    // 7. RECIBIR FILTROS DESDE catalogo.js

    document.addEventListener(
        "catalogo:filtros-cambiados",
        (evento) => {

            filtrosActuales = {
                genero: evento.detail.genero,
                categoria: evento.detail.categoria
            };

            mostrarTodos = false;

            renderizarProductos();
        }
    );
    
    // 8. BOTÓN VER TODO

    botonVerTodo.addEventListener("click", () => {
        mostrarTodos = !mostrarTodos;
        renderizarProductos();
    });

    // 9. PRIMERA CARGA

    renderizarProductos();
});
