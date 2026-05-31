package com.derlys.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseMigration {

    private DatabaseMigration() {}

    public static void migrate(Connection conn) throws SQLException {
        addColumnIfMissing(conn, "usuarios", "username", "TEXT");
        addColumnIfMissing(conn, "usuarios", "telefono", "TEXT");
        backfillUsername(conn);
        createUniqueIndexIfMissing(conn, "idx_usuarios_username", "usuarios", "username");
    }

    private static void addColumnIfMissing(Connection conn, String table, String column, String type)
            throws SQLException {
        if (columnExists(conn, table, column)) {
            return;
        }
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (var st = conn.prepareStatement("PRAGMA table_info(" + table + ")")) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("name"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void backfillUsername(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                    """
                    UPDATE usuarios
                    SET username = LOWER(TRIM(nombre))
                    WHERE username IS NULL OR TRIM(username) = ''
                    """);
            st.executeUpdate(
                    """
                    UPDATE usuarios
                    SET username = LOWER(SUBSTR(email, 1, INSTR(email, '@') - 1))
                    WHERE (username IS NULL OR TRIM(username) = '')
                      AND email IS NOT NULL AND INSTR(email, '@') > 1
                    """);
            st.executeUpdate(
                    """
                    UPDATE usuarios
                    SET username = 'user_' || id
                    WHERE username IS NULL OR TRIM(username) = ''
                    """);
        }
    }

    private static void createUniqueIndexIfMissing(
            Connection conn, String indexName, String table, String column) throws SQLException {
        if (indexExists(conn, indexName)) {
            return;
        }
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE UNIQUE INDEX " + indexName + " ON " + table + " (" + column + ")");
        }
    }

    private static boolean indexExists(Connection conn, String indexName) throws SQLException {
        try (var st = conn.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type = 'index' AND name = ?")) {
            st.setString(1, indexName);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }
}
