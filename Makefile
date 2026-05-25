# App de terminal Java (Maven + SQLite)
.PHONY: dev build run test clean help

MAIN_CLASS  := com.derlys.App
SQLITE_DB   ?= datos/pruebas.db
MVN         := mvn -q

## Compila y ejecuta la app (modo desarrollo)
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

help:
	@echo "Comandos:"
	@echo "  make dev    - compila y ejecuta la app de terminal"
	@echo "  make build  - solo compila"
	@echo "  make run    - ejecuta (sin compilar)"
	@echo "  make test   - ejecuta tests"
	@echo "  make clean  - limpia target y datos/"
	@echo ""
	@echo "Variables:"
	@echo "  SQLITE_DB=$(SQLITE_DB)"
