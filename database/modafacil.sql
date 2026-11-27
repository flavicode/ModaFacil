-- =====================================================
--  ModaFacil - Base de datos (MySQL 8 / MariaDB)
--  Ejecutar:  mysql -u root -p < modafacil.sql
-- =====================================================
DROP DATABASE IF EXISTS modafacil;
CREATE DATABASE modafacil CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE modafacil;

CREATE TABLE usuarios (
    id_usuario      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    correo          VARCHAR(120) NOT NULL UNIQUE,
    contrasena      VARCHAR(255) NOT NULL,           -- HexCrypter.encrypt(...)
    telefono        VARCHAR(15)  NULL,
    fecha_registro  DATE NOT NULL DEFAULT (CURRENT_DATE),
    rol             ENUM('ADMIN','USUARIO') NOT NULL DEFAULT 'USUARIO'
);

CREATE TABLE proveedores (
    id_proveedor  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    ruc           VARCHAR(11)  NULL UNIQUE,
    contacto      VARCHAR(100) NULL,
    telefono      VARCHAR(20)  NULL
);

CREATE TABLE categorias (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL UNIQUE,
    color        VARCHAR(7)  NOT NULL DEFAULT '#C65F64'
);

CREATE TABLE categoria_genero (
    id_categoria BIGINT NOT NULL,
    genero ENUM('Hombre', 'Mujer') NOT NULL,
    PRIMARY KEY (id_categoria, genero),
    FOREIGN KEY (id_categoria)
        REFERENCES categorias(id_categoria)
        ON DELETE CASCADE
);

CREATE TABLE productos (
    id_producto  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_categoria BIGINT NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    imagen       VARCHAR(120) NULL,
    genero       ENUM('Hombre','Mujer','Unisex') NOT NULL,
    descripcion  VARCHAR(255) NULL,
    material     VARCHAR(60)  NULL,
    tipo_tela    VARCHAR(60)  NULL,
    ajuste       VARCHAR(40)  NULL,
    manga        VARCHAR(40)  NULL,
    cuello       VARCHAR(60)  NULL,
    ocasion      VARCHAR(60)  NULL,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);


-- Relación producto - proveedor: cada proveedor vende el producto a su precio y con su stock
CREATE TABLE ofertas (
    id_producto   BIGINT NOT NULL,
    id_proveedor BIGINT NOT NULL,
    precio        DECIMAL(8,2) NOT NULL CHECK (precio > 0),
    PRIMARY KEY (id_producto, id_proveedor),
    FOREIGN KEY (id_producto)   REFERENCES productos(id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor)
);

CREATE TABLE producto_tallas (
    id_producto BIGINT NOT NULL,
    id_proveedor BIGINT NOT NULL,
    talla       VARCHAR(5) NOT NULL,
    stock         INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    PRIMARY KEY (id_producto, id_proveedor, talla),
    FOREIGN KEY (id_producto, id_proveedor) REFERENCES ofertas(id_producto, id_proveedor) ON DELETE CASCADE
);

-- Carrito guardado por usuario (hoy vive en la sesión HTTP). Un item = producto + proveedor + talla
CREATE TABLE carrito_items (
    id_usuario    BIGINT NOT NULL,
    id_producto   BIGINT NOT NULL,
    id_proveedor BIGINT NOT NULL,
    talla         VARCHAR(5) NOT NULL,
    cantidad      INT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (id_usuario, id_producto, id_proveedor, talla),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_producto, id_proveedor, talla) REFERENCES producto_tallas(id_producto, id_proveedor, talla)
);

-- Cabecera de la venta / orden de compra
CREATE TABLE ventas (
    id_venta          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario        BIGINT NOT NULL,
    fecha_venta       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total             DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    estado            ENUM('PENDIENTE', 'PAGADO', 'CANCELADO') NOT NULL DEFAULT 'PAGADO',
    metodo_pago       VARCHAR(40) NOT NULL DEFAULT 'IZIPAY',
    transaccion_id    VARCHAR(100) NULL, -- Código de operación simulado de Izipay
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- Detalle de los productos comprados en cada venta
CREATE TABLE detalle_ventas (
    id_detalle        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_venta          BIGINT NOT NULL,
    id_producto       BIGINT NOT NULL,
    id_proveedor      BIGINT NOT NULL,
    talla             VARCHAR(5) NOT NULL,
    cantidad          INT NOT NULL CHECK (cantidad > 0),
    precio_unitario   DECIMAL(8,2) NOT NULL CHECK (precio_unitario > 0),
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto, id_proveedor, talla) 
        REFERENCES producto_tallas(id_producto, id_proveedor, talla)
);


-- =============== DATOS (los mismos que CatalogoService / UsuarioRepository) ===============
INSERT INTO usuarios (nombre, correo, contrasena, telefono, fecha_registro, rol) VALUES
 ('Admin',          'admin@gmail.com', '35333431363436643639366532343333', null, '2026-08-22', 'ADMIN'),
 ('Diego León',     'diegoleon6431@gmail.com', '34343639363536373566333033323335',         '987654321', '2026-08-22', 'USUARIO'),
 ('Thirsa Alvarez', 'thirsa18utp@gmail.com',   '353436383639373237333631356633313338',   '999888777', '2026-09-14', 'USUARIO'),
 ('Dana Quiñones',  'dana@gmail.com',          '34343631366536313635356633323336',       '987654567', '2026-10-01', 'USUARIO');
/* A ,Dieg_025, Thirsa_18, Danae_26, SAdmin$3 */
INSERT INTO categorias (nombre, color) VALUES ('Polos','#C65F64'),('Pantalones','#C65F64'),('Vestidos','#C65F64');

USE modafacil;

INSERT INTO categorias (nombre, color) VALUES
('Camisas', '#C65F64'),
('Casacas', '#C65F64'),
('Shorts', '#C65F64'),
('Jeans', '#C65F64'),
('Poleras', '#C65F64'),
('Ropa deportiva', '#C65F64'),
('Blusas', '#C65F64'),
('Faldas', '#C65F64'),
('Chaquetas', '#C65F64'),
('Tops', '#C65F64')
ON DUPLICATE KEY UPDATE nombre = categorias.nombre;

INSERT IGNORE INTO categoria_genero (id_categoria, genero)
SELECT id_categoria, 'Hombre'
FROM categorias
WHERE nombre IN (
    'Polos',
    'Pantalones',
    'Camisas',
    'Casacas',
    'Shorts',
    'Jeans',
    'Poleras',
    'Ropa deportiva'
);

INSERT IGNORE INTO categoria_genero (id_categoria, genero)
SELECT id_categoria, 'Mujer'
FROM categorias
WHERE nombre IN (
    'Vestidos',
    'Blusas',
    'Pantalones',
    'Faldas',
    'Chaquetas',
    'Polos',
    'Jeans',
    'Tops',
    'Ropa deportiva'
);

INSERT INTO proveedores (nombre, ruc, contacto, telefono) VALUES
 ('Metro',      '20100070970', 'María Quispe',    '987 654 321'),
 ('PlazaVea',   '20100038219', 'Carlos Mendoza',  '986 321 654'),
 ('Falabella',  '20333363002', 'Lucía Torres',    '999 876 543'),
 ('Ripley',     '20337564373', 'Jorge Huamán',    '987 112 334'),
 ('Oechsle',    '20100128056', 'Ana Ramírez',     '985 667 788'),
 ('Topitop',    '20378415221', 'Luis Paredes',    '984 221 908');

INSERT INTO productos (id_categoria, nombre, imagen, genero, descripcion, material, tipo_tela, ajuste, manga, cuello, ocasion) VALUES
 (1,'Polo Básico Negro para Hombre','p_camisero_1.jpg','Hombre',
  'Polo clásico de algodón, cómodo y versátil. Ideal para el día a día.',
  'Algodón 100%','Piqué','Regular','Corta','Clásico con botones','Casual / Diario'),
 (2,'Pantalón Slim Fit Negro','pantalon_slim.jpeg','Hombre',
  'Pantalón negro de corte slim fit, moderno, cómodo y versátil para diferentes ocasiones.',
  'Algodón y elastano','Denim elástico','Slim Fit','No aplica','No aplica','Casual / Semiformal'),
 (3,'Vestido Casual Verano','vestido.jpeg','Mujer',
  'Vestido casual de verano, ligero y cómodo, ideal para días cálidos y ocasiones informales.',
  'Algodón y poliéster','Tela ligera','Regular','Corta','Redondo','Casual / Verano');

INSERT INTO ofertas (id_producto, id_proveedor, precio) VALUES
 (1, 1, 39.90), (1, 2, 45.00), (1, 3, 42.00),
 (2, 1, 25.00), (2, 2, 28.00), (2, 3, 32.00), (2, 4, 27.00), (2, 5, 30.00), (2, 6, 29.00),
 (3, 1, 42.00), (3, 2, 48.00), (3, 3, 50.00), (3, 4, 46.00), (3, 5, 55.00), (3, 6, 49.00);

INSERT INTO producto_tallas (id_producto, id_proveedor, talla, stock) VALUES
 -- Producto 1: Polo Básico (Proveedores 1, 2 y 3)
 (1, 1, 'S',  8), (1, 1, 'M', 12), (1, 1, 'L',  5), (1, 1, 'XL', 0), (1, 1, 'XXL', 3),
 (1, 2, 'S',  4), (1, 2, 'M',  6), (1, 2, 'L',  2), (1, 2, 'XL', 5), (1, 2, 'XXL', 0),
 (1, 3, 'S', 10), (1, 3, 'M',  0), (1, 3, 'L',  8), (1, 3, 'XL', 4), (1, 3, 'XXL', 1),

 -- Producto 2: Pantalón Slim Fit (Proveedores 1 al 6)
 (2, 1, '28', 6), (2, 1, '30',  8), (2, 1, '32', 4), (2, 1, '34', 0), (2, 1, '36', 2),
 (2, 2, '28', 3), (2, 2, '30',  5), (2, 2, '32', 7), (2, 2, '34', 2), (2, 2, '36', 0),
 (2, 3, '28', 0), (2, 3, '30',  4), (2, 3, '32', 6), (2, 3, '34', 3), (2, 3, '36', 1),
 (2, 4, '28', 5), (2, 4, '30', 10), (2, 4, '32', 2), (2, 4, '34', 0), (2, 4, '36', 4),
 (2, 5, '28', 2), (2, 5, '30',  3), (2, 5, '32', 5), (2, 5, '34', 4), (2, 5, '36', 2),
 (2, 6, '28', 4), (2, 6, '30',  6), (2, 6, '32', 0), (2, 6, '34', 5), (2, 6, '36', 3),

 -- Producto 3: Vestido Casual Verano (Proveedores 1 al 6 - Agotado en todos para probar estado "Agotado")
 (3, 1, 'XS', 0), (3, 1, 'S', 0), (3, 1, 'M', 0), (3, 1, 'L', 0), (3, 1, 'XL', 0),
 (3, 2, 'XS', 0), (3, 2, 'S', 0), (3, 2, 'M', 0), (3, 2, 'L', 0), (3, 2, 'XL', 0),
 (3, 3, 'XS', 0), (3, 3, 'S', 0), (3, 3, 'M', 0), (3, 3, 'L', 0), (3, 3, 'XL', 0),
 (3, 4, 'XS', 0), (3, 4, 'S', 0), (3, 4, 'M', 0), (3, 4, 'L', 0), (3, 4, 'XL', 0),
 (3, 5, 'XS', 0), (3, 5, 'S', 0), (3, 5, 'M', 0), (3, 5, 'L', 0), (3, 5, 'XL', 0),
 (3, 6, 'XS', 0), (3, 6, 'S', 0), (3, 6, 'M', 0), (3, 6, 'L', 0), (3, 6, 'XL', 0);
