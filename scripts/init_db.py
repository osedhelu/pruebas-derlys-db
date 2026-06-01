#!/usr/bin/env python3
"""Aplica datos/init_database.sql sin necesitar sqlite3 en PATH."""

from pathlib import Path
import sqlite3
import sys

ROOT = Path(__file__).resolve().parents[1]
SQL_FILE = ROOT / "datos" / "init_database.sql"
DB_FILE = ROOT / "datos" / "pruebas.db"


def main() -> int:
    if not SQL_FILE.is_file():
        print(f"No se encontró: {SQL_FILE}", file=sys.stderr)
        return 1

    DB_FILE.parent.mkdir(parents=True, exist_ok=True)
    if DB_FILE.exists():
        DB_FILE.unlink()

    conn = sqlite3.connect(DB_FILE)
    try:
        conn.executescript(SQL_FILE.read_text(encoding="utf-8"))
        conn.commit()
    finally:
        conn.close()

    print(f"Base creada: {DB_FILE}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
