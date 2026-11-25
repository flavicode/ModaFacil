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
    id_importador BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE categorias (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL UNIQUE,
    color        VARCHAR(7)  NOT NULL DEFAULT '#C65F64'
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
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE producto_tallas (
    id_producto BIGINT NOT NULL,
    talla       VARCHAR(5) NOT NULL,
    PRIMARY KEY (id_producto, talla),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
);

-- Relación producto - importador: cada importador vende el producto a su precio y con su stock
CREATE TABLE ofertas (
    id_producto   BIGINT NOT NULL,
    id_importador BIGINT NOT NULL,
    precio        DECIMAL(8,2) NOT NULL CHECK (precio > 0),
    stock         INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    PRIMARY KEY (id_producto, id_importador),
    FOREIGN KEY (id_producto)   REFERENCES productos(id_producto),
    FOREIGN KEY (id_importador) REFERENCES proveedores(id_importador)
);

-- Carrito guardado por usuario (hoy vive en la sesión HTTP). Un item = producto + importador + talla
CREATE TABLE carrito_items (
    id_usuario    BIGINT NOT NULL,
    id_producto   BIGINT NOT NULL,
    id_importador BIGINT NOT NULL,
    talla         VARCHAR(5) NOT NULL,
    cantidad      INT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (id_usuario, id_producto, id_importador, talla),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_producto, id_importador) REFERENCES ofertas(id_producto, id_importador),
    FOREIGN KEY (id_producto, talla) REFERENCES producto_tallas(id_producto, talla)
);

-- =============== DATOS (los mismos que CatalogoService / UsuarioRepository) ===============
INSERT INTO usuarios (nombre, correo, contrasena, telefono, fecha_registro, rol) VALUES
 ('Admin',          'admin@gmail.com', '35333431363436643639366532343333', null, '2026-08-22', 'ADMIN'),
 ('Diego León',     'diegoleon6431@gmail.com', '34343639363536373566333033323335',         '987654321', '2026-08-22', 'USUARIO'),
 ('Thirsa Alvarez', 'thirsa18utp@gmail.com',   '353436383639373237333631356633313338',   '999888777', '2026-09-14', 'USUARIO'),
 ('Dana Quiñones',  'dana@gmail.com',          '34343631366536313635356633323336',       '987654567', '2026-10-01', 'USUARIO');

INSERT INTO categorias (nombre, color) VALUES ('Polos','#C65F64'),('Pantalones','#C65F64'),('Vestidos','#C65F64');

INSERT INTO proveedores (nombre) VALUES
 ('Importador 1'),('Importador 2'),('Importador 3'),('Importador 4'),
 ('Importador 5'),('Importador 6'),('Importador 7'),('Importador 8');

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

INSERT INTO producto_tallas (id_producto, talla) VALUES
 (1,'S'),(1,'M'),(1,'L'),(1,'XL'),(1,'XXL'),
 (2,'28'),(2,'30'),(2,'32'),(2,'34'),(2,'36'),
 (3,'XS'),(3,'S'),(3,'M'),(3,'L'),(3,'XL');

-- stock según importador: 1→20, 2→8, 3→1, 4→0, 5→15, 6→5, 7→12, 8→3
INSERT INTO ofertas (id_producto, id_importador, precio, stock) VALUES
 (1,1,39.90,20),(1,2,45.00, 8),(1,3,42.00, 1),
 (2,1,25.00,20),(2,2,28.00, 8),(2,3,32.00, 1),(2,4,27.00, 0),
 (2,5,30.00,15),(2,6,29.00, 5),(2,7,35.00,12),(2,8,31.00, 3),
 (3,1,42.00,20),(3,2,48.00, 8),(3,3,50.00, 1),(3,4,46.00, 0),(3,5,55.00,15),(3,6,49.00, 5);
