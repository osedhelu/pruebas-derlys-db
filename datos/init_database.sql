
PRAGMA foreign_keys = ON;

-- -----------------------------------------------------------------------------
-- Eliminar tablas existentes (orden por dependencias)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS asientos_contables;
DROP TABLE IF EXISTS transacciones;
DROP TABLE IF EXISTS preventas;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS lotes;
DROP TABLE IF EXISTS plan_cuentas;
DROP TABLE IF EXISTS tipos_movimiento;
DROP TABLE IF EXISTS roles;

-- -----------------------------------------------------------------------------
-- 1. Roles
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE
);

INSERT INTO roles (id, nombre) VALUES
    (1, 'administrador'),
    (2, 'granjero'),
    (3, 'vendedor'),
    (4, 'cliente');

-- -----------------------------------------------------------------------------
-- 2. Usuarios (staff + clientes)
--    - username: login (único, obligatorio)
--    - nombre:   nombre para mostrar / quién es
--    - telefono: contacto (importante en clientes)
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT NOT NULL,
    username       TEXT NOT NULL UNIQUE,
    email          TEXT UNIQUE,
    telefono       TEXT,
    password_hash  TEXT,
    rol_id         INTEGER NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

CREATE UNIQUE INDEX idx_usuarios_username ON usuarios (username);

-- Staff
INSERT INTO usuarios (id, nombre, username, email, telefono, password_hash, rol_id, fecha_creacion) VALUES
    (1, 'Administrador', 'admin',  'admin@granja.local',  NULL,         '1234', 1, datetime('now')),
    (2, 'Operador Granja', 'granja', 'granja@granja.local', NULL,       '1234', 2, datetime('now')),
    (3, 'Vendedor', 'ventas', 'ventas@granja.local', NULL,           '1234', 3, datetime('now'));

-- Clientes de ejemplo (con teléfono para preventas / llamadas)
INSERT INTO usuarios (id, nombre, username, email, telefono, password_hash, rol_id, fecha_creacion) VALUES
    (4, 'Juan Pérez',    'juanperez',  'juan@ejemplo.com',   '3001112233', '1234', 4, datetime('now')),
    (5, 'María Gómez',   'mariagomez', 'maria@ejemplo.com',  '3004445566', '1234', 4, datetime('now')),
    (6, 'Pedro Cliente', 'pedro',      NULL,                 '3007778899', '1234', 4, datetime('now'));

-- -----------------------------------------------------------------------------
-- 3. Plan de cuentas (contabilidad)
--    IDs fijos: la app usa 1=Caja, 3=Gasto medicinas, 4=Ingresos, 5=Equipos
-- -----------------------------------------------------------------------------
CREATE TABLE plan_cuentas (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo     TEXT NOT NULL UNIQUE,
    nombre     TEXT NOT NULL,
    naturaleza TEXT NOT NULL CHECK (naturaleza IN ('debito', 'credito'))
);

INSERT INTO plan_cuentas (id, codigo, nombre, naturaleza) VALUES
    (1, '1105', 'Caja General',                              'debito'),
    (2, '5105', 'Gasto Alimento',                          'debito'),
    (3, '5110', 'Gasto Medicinas',                         'debito'),
    (4, '4135', 'Ingresos por Ventas',                     'credito'),
    (5, '1524', 'Equipos de Avicultura (Bebederos, etc.)', 'debito'),
    (6, '3115', 'Aportes Sociales (Capital del dueño)',    'credito'),
    (7, '1430', 'Inventario de Pollos (Activos Biológicos)', 'debito');

-- -----------------------------------------------------------------------------
-- 4. Tipos de movimiento
--    IDs fijos: 1=VENTA (usado al cobrar preventas), 2=MUERTE, etc.
-- -----------------------------------------------------------------------------
CREATE TABLE tipos_movimiento (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT NOT NULL UNIQUE,
    descripcion TEXT
);

INSERT INTO tipos_movimiento (id, nombre, descripcion) VALUES
    (1, 'VENTA',            'Salida de pollos por comercialización'),
    (2, 'MUERTE',           'Baja de pollos por mortalidad'),
    (3, 'GASTO_OPERATIVO',  'Compra de insumos: alimento, medicina, etc.'),
    (4, 'INVERSION_ACTIVO', 'Compra de equipos: bebederos, mallas, abanicos'),
    (5, 'APORTE_CAPITAL',   'Inyección de dinero por parte del dueño'),
    (6, 'COMPRA_LOTE',      'Compra inicial del lote de pollos');

-- -----------------------------------------------------------------------------
-- 5. Lotes
-- -----------------------------------------------------------------------------
CREATE TABLE lotes (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_lote      TEXT NOT NULL UNIQUE,
    fecha_entrada    DATE NOT NULL,
    cantidad_inicial INTEGER NOT NULL CHECK (cantidad_inicial > 0),
    raza             TEXT,
    estado           TEXT DEFAULT 'activo',
    observaciones    TEXT,
    costo_inicial    REAL NOT NULL DEFAULT 0 CHECK (costo_inicial >= 0)
);

INSERT INTO lotes (id, codigo_lote, fecha_entrada, cantidad_inicial, raza, estado, observaciones, costo_inicial) VALUES
    (1, 'LT-001', date('now', '-45 days'), 500, 'Ross 308', 'activo', 'Lote inicial de demostración', 2500000),
    (2, 'LT-002', date('now', '-20 days'), 200, 'Ross 308', 'activo', 'Segundo lote de demostración', 1200000);

-- -----------------------------------------------------------------------------
-- 6. Transacciones (movimientos por lote)
-- -----------------------------------------------------------------------------
CREATE TABLE transacciones (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha               DATETIME DEFAULT CURRENT_TIMESTAMP,
    descripcion         TEXT NOT NULL,
    lote_id             INTEGER NOT NULL,
    usuario_id          INTEGER NOT NULL,
    tipo_movimiento_id  INTEGER NOT NULL,
    cantidad_unidades   INTEGER DEFAULT 0 CHECK (cantidad_unidades >= 0),
    comprobante_url     TEXT,
    FOREIGN KEY (lote_id) REFERENCES lotes(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (tipo_movimiento_id) REFERENCES tipos_movimiento(id)
);

CREATE INDEX idx_transacciones_lote ON transacciones (lote_id);
CREATE INDEX idx_transacciones_fecha ON transacciones (fecha);

-- -----------------------------------------------------------------------------
-- 7. Asientos contables (partida doble por transacción)
-- -----------------------------------------------------------------------------
CREATE TABLE asientos_contables (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    transaccion_id INTEGER NOT NULL,
    cuenta_id      INTEGER NOT NULL,
    debe           REAL DEFAULT 0.00 CHECK (debe >= 0),
    haber          REAL DEFAULT 0.00 CHECK (haber >= 0),
    FOREIGN KEY (transaccion_id) REFERENCES transacciones(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES plan_cuentas(id),
    CHECK (debe >= 0 AND haber >= 0)
);

CREATE INDEX idx_asientos_transaccion ON asientos_contables (transaccion_id);
CREATE INDEX idx_asientos_cuenta ON asientos_contables (cuenta_id);

-- -----------------------------------------------------------------------------
-- 8. Preventas
--    Estados: pendiente | listo | mora | completada | entregado
--    (completada = cobrada con venta contable; entregado = ya entregó al cliente)
-- -----------------------------------------------------------------------------
CREATE TABLE preventas (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id        INTEGER NOT NULL,
    lote_id           INTEGER NOT NULL,
    cantidad_apartada INTEGER NOT NULL CHECK (cantidad_apartada > 0),
    fecha_apartado    DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado            TEXT DEFAULT 'pendiente',
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

CREATE INDEX idx_preventas_cliente ON preventas (cliente_id);
CREATE INDEX idx_preventas_lote ON preventas (lote_id);
CREATE INDEX idx_preventas_estado ON preventas (estado);

-- Preventa de ejemplo (pendiente — aparece teléfono en tabla de ventas)
INSERT INTO preventas (id, cliente_id, lote_id, cantidad_apartada, fecha_apartado, estado) VALUES
    (1, 4, 1, 50, datetime('now', '-2 days'), 'pendiente');

-- -----------------------------------------------------------------------------
-- Ejemplo opcional: venta y gasto con asientos (descomenta si quieres datos contables)
-- -----------------------------------------------------------------------------
/*
INSERT INTO transacciones (descripcion, lote_id, usuario_id, tipo_movimiento_id, cantidad_unidades)
VALUES ('Venta demo 10 pollos', 1, 3, 1, 10);
INSERT INTO asientos_contables (transaccion_id, cuenta_id, debe, haber) VALUES
    (last_insert_rowid(), 1, 500000, 0),
    (last_insert_rowid(), 4, 0, 500000);
*/

-- -----------------------------------------------------------------------------
-- Reiniciar secuencias AUTOINCREMENT
-- -----------------------------------------------------------------------------
DELETE FROM sqlite_sequence;
INSERT INTO sqlite_sequence (name, seq) VALUES
    ('roles',            4),
    ('usuarios',         6),
    ('plan_cuentas',     7),
    ('tipos_movimiento', 6),
    ('lotes',            2),
    ('transacciones',    0),
    ('asientos_contables', 0),
    ('preventas',        1);

-- -----------------------------------------------------------------------------
-- Verificación rápida
-- -----------------------------------------------------------------------------
SELECT 'OK: roles' AS check_, COUNT(*) AS n FROM roles;
SELECT 'OK: usuarios' AS check_, COUNT(*) AS n FROM usuarios;
SELECT 'OK: plan_cuentas' AS check_, COUNT(*) AS n FROM plan_cuentas;
SELECT 'OK: tipos_movimiento' AS check_, COUNT(*) AS n FROM tipos_movimiento;
SELECT 'OK: lotes' AS check_, COUNT(*) AS n FROM lotes;
SELECT 'OK: preventas' AS check_, COUNT(*) AS n FROM preventas;
