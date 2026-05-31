package com.derlys;

import java.io.IOException;
import java.sql.SQLException;
import com.derlys.config.DatabaseConfig;
import com.derlys.db.DatabaseConnection;
import com.derlys.model.Transaccion;
import com.derlys.model.Usuario;
import com.derlys.repository.AuthRepository;
import com.derlys.repository.TransaccionRepository;
import com.derlys.ui.LoginScreen;
import java.util.List;



public class App {
    public static void main(String[] args) throws SQLException, IOException {
        DatabaseConfig config = DatabaseConfig.load();
        DatabaseConnection connection = new DatabaseConnection(config);
       
        try (var conn = connection.connect()) {
            new LoginScreen(conn).run();
            
        }
    }
}