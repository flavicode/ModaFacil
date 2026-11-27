let modalStockInstance = null;

            function abrirModalStock(btn) {
                const id = btn.getAttribute('data-id');
                const nombre = btn.getAttribute('data-nombre');
                const codigo = btn.getAttribute('data-codigo');
                const categoria = btn.getAttribute('data-categoria');
                const imagen = btn.getAttribute('data-imagen');
                const totalActual = btn.getAttribute('data-total');

                document.getElementById('modalProductoId').value = id;
                document.getElementById('modalProductoNombre').textContent = nombre;
                document.getElementById('modalProductoCodigo').textContent = codigo;
                document.getElementById('modalProductoCategoria').textContent = categoria;
                document.getElementById('modalStockActualTexto').textContent = totalActual + ' unidades';

                const imgEl = document.getElementById('modalProductoImg');
                imgEl.src = imagen;
                imgEl.onerror = function() {
                    this.src = 'https://placehold.co/132x132/f4f4f4/c0a0a0?text=Prenda';
                };

                const spansTallas = btn.parentElement.querySelectorAll('.datos-tallas-producto span');
                const thead = document.getElementById('modalMatrizHead');
                const tbody = document.getElementById('modalMatrizBody');
                const hiddenContainer = document.getElementById('modalInputsOcultos');

                thead.innerHTML = '';
                tbody.innerHTML = '';
                hiddenContainer.innerHTML = '';

                // Agrupar datos por proveedor manteniendo el orden y recolectar tallas únicas
                const proveedoresMap = new Map();
                const tallasSet = new Set();

                spansTallas.forEach(span => {
                    const idProveedor = span.getAttribute('data-id-proveedor');
                    const proveedor = span.getAttribute('data-proveedor');
                    const talla = span.getAttribute('data-talla');
                    const stock = parseInt(span.getAttribute('data-stock'), 10) || 0;

                    tallasSet.add(talla);

                    if (!proveedoresMap.has(idProveedor)) {
                        proveedoresMap.set(idProveedor, {
                            idProveedor: idProveedor,
                            nombreProveedor: proveedor,
                            stocksPorTalla: new Map()
                        });
                    }
                    proveedoresMap.get(idProveedor).stocksPorTalla.set(talla, stock);
                });

                const tallasOrdenadas = Array.from(tallasSet);

                // Encabezado de columnas: Proveedor + cada talla
                let headHtml = '<tr><th class="col-proveedor-header"><i class="bi bi-people-fill me-2"></i>Proveedor</th>';
                tallasOrdenadas.forEach(talla => {
                    headHtml += `<th><i class="bi bi-tag-fill me-1 small opacity-75"></i>${talla}</th>`;
                });
                headHtml += '</tr>';
                thead.innerHTML = headHtml;

                // Filas por proveedor y creación de inputs hidden sincronizados
                let idx = 0;
                proveedoresMap.forEach(imp => {
                    const tr = document.createElement('tr');

                    const tdNombre = document.createElement('td');
                    tdNombre.className = 'matriz-proveedor-nombre';
                    tdNombre.textContent = imp.nombreProveedor;
                    tr.appendChild(tdNombre);

                    tallasOrdenadas.forEach(talla => {
                        const stock = imp.stocksPorTalla.has(talla) ? imp.stocksPorTalla.get(talla) : 0;
                        const inputKey = `celda-stock-${idx}`;
                        idx++;

                        // Inputs hidden para enviar listas paralelas al controlador
                        const hProv = document.createElement('input');
                        hProv.type = 'hidden';
                        hProv.name = 'proveedores';
                        hProv.value = imp.idProveedor;

                        const hTalla = document.createElement('input');
                        hTalla.type = 'hidden';
                        hTalla.name = 'tallas';
                        hTalla.value = talla;

                        const hCant = document.createElement('input');
                        hCant.type = 'hidden';
                        hCant.name = 'cantidades';
                        hCant.value = stock;
                        hCant.setAttribute('data-hidden-for', inputKey);

                        hiddenContainer.append(hProv, hTalla, hCant);

                        const td = document.createElement('td');
                        const claseStock = stock > 0 ? 'celda-con-stock' : '';
                        td.innerHTML = `
                            <div class="matriz-stepper ${claseStock}">
                                <button type="button" onclick="cambiarCantidadMatriz(this, -1)" aria-label="Disminuir">−</button>
                                <input type="number" class="input-matriz-stock" data-key="${inputKey}"
                                       value="${stock}" min="0" oninput="sincronizarCeldaMatriz(this)">
                                <button type="button" onclick="cambiarCantidadMatriz(this, 1)" aria-label="Aumentar">+</button>
                            </div>
                        `;
                        tr.appendChild(td);
                    });

                    tbody.appendChild(tr);
                });

                if (!modalStockInstance) {
                    modalStockInstance = new bootstrap.Modal(document.getElementById('modalActualizarStock'));
                }
                modalStockInstance.show();
            }

            function cambiarCantidadMatriz(boton, delta) {
                const stepper = boton.closest('.matriz-stepper');
                const input = stepper.querySelector('.input-matriz-stock');
                const valorActual = parseInt(input.value, 10) || 0;
                const nuevoValor = Math.max(0, valorActual + delta);
                input.value = nuevoValor;
                sincronizarCeldaMatriz(input);
            }

            function sincronizarCeldaMatriz(input) {
                let val = parseInt(input.value, 10);
                if (isNaN(val) || val < 0) {
                    val = 0;
                    input.value = 0;
                }

                const stepper = input.closest('.matriz-stepper');
                stepper.classList.toggle('celda-con-stock', val > 0);

                const key = input.getAttribute('data-key');
                const hiddenInput = document.querySelector(`#modalInputsOcultos input[data-hidden-for="${key}"]`);
                if (hiddenInput) {
                    hiddenInput.value = val;
                }
            }

            document.addEventListener('DOMContentLoaded', () => {
                const inputBusqueda = document.getElementById('filtroBusqueda');
                const selectEstado = document.getElementById('filtroEstado');
                const selectCategoria = document.getElementById('filtroCategoria');
                const btnReset = document.getElementById('btnResetFiltros');
                const filas = document.querySelectorAll('.fila-producto');
                const parametros = new URLSearchParams(window.location.search);
                const idProducto = parametros.get('idProducto');

                function aplicarFiltros() {
                    const texto = inputBusqueda.value.trim().toLowerCase();
                    const estado = selectEstado.value;
                    const categoria = selectCategoria.value;
                    let visibles = 0;

                    filas.forEach(fila => {
                        const nombre = fila.getAttribute('data-nombre');
                        const codigo = fila.getAttribute('data-codigo');
                        const cat = fila.getAttribute('data-categoria');
                        const est = fila.getAttribute('data-estado');

                        const coincideTexto = nombre.includes(texto) || codigo.includes(texto);
                        const coincideEstado = (estado === 'todos') || (est === estado);
                        const coincideCat = (categoria === 'todas') || (cat === categoria);

                        if (coincideTexto && coincideEstado && coincideCat) {
                            fila.style.display = '';
                            visibles++;
                        } else {
                            fila.style.display = 'none';
                        }
                    });

                    document.getElementById('contadorResultados').textContent =
                        `Mostrando ${visibles} de ${filas.length} productos`;
                }

                inputBusqueda.addEventListener('input', aplicarFiltros);
                selectEstado.addEventListener('change', aplicarFiltros);
                selectCategoria.addEventListener('change', aplicarFiltros);

                btnReset.addEventListener('click', () => {
                    inputBusqueda.value = '';
                    selectEstado.value = 'todos';
                    selectCategoria.value = 'todas';
                    const url = new URL(window.location.href);
                    url.searchParams.delete('idProducto');
                    window.history.replaceState({}, '', url);
                    aplicarFiltros();
                });

                // FILTRAR PRODUCTO AL INGRESAR DESDE PRODUCTOS
                if (idProducto) {

                    const botonProducto = Array.from(
                        document.querySelectorAll('.btn-actualizar-stock')
                    ).find(boton => boton.dataset.id === idProducto);

                    if (botonProducto) {
                        inputBusqueda.value = botonProducto.dataset.codigo;
                        aplicarFiltros();
                    } else {
                        alert("El producto no está disponible en el inventario.");
                    }
                }
            });