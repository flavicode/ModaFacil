// 1. OBTENER PRODUCTOS DEL SERVIDOR

    const relaciones = Array.isArray(window.relacionesServidor)
        ? window.relacionesServidor
        : [];

        console.log("Relaciones recibidas:", relaciones.length);

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
                genero: p.genero || "",
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

    window.productosCatalogo =Array.from(mapaProductos.values());
    console.log("Productos preparados:",window.productosCatalogo.length);