-- Renombrar categorías legacy a los 4 tipos oficiales de vinculación
UPDATE documentos_practica SET categoria = 'CARTA_PRESENTACION' WHERE categoria = 'VINCULACION';
UPDATE documentos_practica SET categoria = 'CONVENIO_PRACTICA' WHERE categoria = 'CONVENIO';
