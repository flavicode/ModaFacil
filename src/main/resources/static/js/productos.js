
document.addEventListener("DOMContentLoaded", () => {

    const contenedor = document.getElementById("contenedor-productos");
    const buscador = document.getElementById("input-buscador");
    const contador = document.getElementById("contador-productos-texto");
    const categoriaTexto = document.getElementById("categoria-actual-texto");
    const sinResultados = document.getElementById("sin-resultados");
    const botonVerTodo = document.getElementById("btn-ver-todo");

    let categoriaActual = "Todas";
    let mostrarTodos = false;

    // 1. OBTENER PRODUCTOS DEL SERVIDOR

    const relaciones = Array.isArray(relacionesServidor)
        ? relacionesServidor
        : [];

    // 2. AGRUPAR PRECIOS Y PROVEEDORES POR PRODUCTO

    const mapaProductos = new Map();

    relaciones.forEach(rel => {

        if (!rel.producto) return;

        const p = rel.producto;
        const id = p.idProducto;

        if (id == null) return;

        if (!mapaProductos.has(id)) {
            mapaProductos.set(id, {
                id: id,
                nombre: p.nombre || "",
                descripcion: p.descripcion || "",
                imagen: p.imagen || "",
                categoria: p.categoria?.nombre || "",
                precios: [],
                proveedores: new Set()
            });
        }

        const producto = mapaProductos.get(id);
        const precio = Number(rel.precio);

        if (rel.precio != null && Number.isFinite(precio)) {
            producto.precios.push(precio);
        }

        if (rel.importador?.idImportador != null) {
            producto.proveedores.add(rel.importador.idImportador);
        }
    });

    const productos = Array.from(mapaProductos.values());

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

        const minimo = Math.min(...producto.precios);
        const maximo = Math.max(...producto.precios);

        if (minimo === maximo) {
            return "Desde " + dinero(minimo);
        }

        return "Desde " + dinero(minimo) +
               " - " + dinero(maximo);
    }

    function obtenerImagen(ruta) {
        if (!ruta) return "";

        if (/^https?:\/\//i.test(ruta)) {
            return ruta;
        }

        if (ruta.startsWith("/")) {
            return ruta;
        }

        return "/images/" + ruta;
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
            cantidad + (cantidad === 1 ? " proveedor" : " proveedores");

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

            const coincideBusqueda =
                normalizar(producto.nombre).includes(texto) ||
                normalizar(producto.categoria).includes(texto) ||
                normalizar(producto.descripcion).includes(texto);

            const categoriaProducto = normalizar(producto.categoria);
            const filtroCategoria = normalizar(categoriaActual);

            const coincideCategoria =
                categoriaActual === "Todas" ||
                categoriaProducto === filtroCategoria ||
                categoriaProducto.includes(filtroCategoria) ||
                filtroCategoria.includes(categoriaProducto);

            return coincideBusqueda && coincideCategoria;
        });

        const visibles = mostrarTodos
            ? filtrados
            : filtrados.slice(0, 6);

        contenedor.replaceChildren();

        visibles.forEach(producto => {
            contenedor.appendChild(crearTarjeta(producto));
        });

        contador.textContent = filtrados.length + " productos";
        categoriaTexto.textContent = categoriaActual;

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

    // 7. BOTONES DE CATEGORÍAS

    document.querySelectorAll(".categoria-btn").forEach(boton => {

        boton.addEventListener("click", () => {

            document.querySelectorAll(".categoria-btn")
                .forEach(b => b.classList.remove("active"));

            boton.classList.add("active");

            categoriaActual = boton.dataset.categoria;
            mostrarTodos = false;

            renderizarProductos();
        });
    });

    // 8. BOTÓN VER TODO

    botonVerTodo.addEventListener("click", () => {
        mostrarTodos = !mostrarTodos;
        renderizarProductos();
    });

    // 9. PRIMERA CARGA

    renderizarProductos();
});
