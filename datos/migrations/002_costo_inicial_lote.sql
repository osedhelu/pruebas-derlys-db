-- Costo total pagado al crear el lote de pollos
ALTER TABLE lotes ADD COLUMN costo_inicial REAL DEFAULT 0;

INSERT INTO tipos_movimiento (nombre, descripcion)
SELECT 'COMPRA_LOTE', 'Compra inicial del lote de pollos'
WHERE NOT EXISTS (SELECT 1 FROM tipos_movimiento WHERE nombre = 'COMPRA_LOTE');
