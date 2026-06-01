-- Monto acordado al entregar (para no olvidar al cobrar)
ALTER TABLE preventas ADD COLUMN monto_a_cobrar REAL;
ALTER TABLE preventas ADD COLUMN notas_entrega TEXT;
