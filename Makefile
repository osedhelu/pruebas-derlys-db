# App Swing Java (Maven + SQLite)
.PHONY: dev build run test clean init-db help

MAIN_CLASS  := com.derlys.App
SQLITE_DB   ?= datos/pruebas.db
MVN         := mvn -q
# La terminal integrada de Cursor/VS Code no hereda DISPLAY; Xwayland suele usar :0
DISPLAY     ?= :0
export DISPLAY

## Compila y ejecuta la app Swing (modo desarrollo)
dev: datos
	$(MVN) compile exec:java -Dexec.mainClass=$(MAIN_CLASS)

## Solo compila
build:
	$(MVN) compile

## Ejecuta sin recompilar (requiere build previo)
run:
	$(MVN) exec:java -Dexec.mainClass=$(MAIN_CLASS)

test:
	$(MVN) test

clean:
	$(MVN) clean
	rm -rf datos

datos:
	@mkdir -p datos

## Recrea la base de datos desde cero (datos/init_database.sql)
init-db: datos
	@if command -v sqlite3 >/dev/null 2>&1; then \
		rm -f $(SQLITE_DB); \
		sqlite3 $(SQLITE_DB) < datos/init_database.sql; \
		echo "Base creada: $(SQLITE_DB)"; \
	else \
		python3 scripts/init_db.py; \
	fi

help:
	@echo "Comandos:"
	@echo "  make dev    - compila y ejecuta la app Swing"
	@echo "  make build  - solo compila"
	@echo "  make run    - ejecuta (sin compilar)"
	@echo "  make test   - ejecuta tests"
	@echo "  make clean   - limpia target y datos/"
	@echo "  make init-db - recrea $(SQLITE_DB) desde cero"
	@echo ""
	@echo "Variables:"
	@echo "  SQLITE_DB=$(SQLITE_DB)"
