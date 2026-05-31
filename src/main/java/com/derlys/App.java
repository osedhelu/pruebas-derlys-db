package com.derlys;

import java.io.IOException;
import java.sql.SQLException;
import com.derlys.config.DatabaseConfig;
import com.derlys.db.DatabaseConnection;
import com.derlys.ui.LoginScreen;

public class App {
    public static void main(String[] args) throws IOException, SQLException {
        DatabaseConfig config = DatabaseConfig.load();
        var conn = new DatabaseConnection(config).connect();
        // No cerrar conn aquí: Swing sigue abierto y el login la necesita después.
        new LoginScreen(conn).setVisible(true);
    }
}
