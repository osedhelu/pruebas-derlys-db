package com.derlys.db;

import com.derlys.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private final String url;

    public DatabaseConnection(DatabaseConfig config) {
        this.url = config.url();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
