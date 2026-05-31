-- Migración manual (la app también aplica esto al iniciar)
ALTER TABLE usuarios ADD COLUMN username TEXT;
ALTER TABLE usuarios ADD COLUMN telefono TEXT;

UPDATE usuarios
SET username = LOWER(TRIM(nombre))
WHERE username IS NULL OR TRIM(username) = '';

UPDATE usuarios
SET username = LOWER(SUBSTR(email, 1, INSTR(email, '@') - 1))
WHERE (username IS NULL OR TRIM(username) = '')
  AND email IS NOT NULL AND INSTR(email, '@') > 1;

UPDATE usuarios
SET username = 'user_' || id
WHERE username IS NULL OR TRIM(username) = '';

CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios (username);
