package com.derlys;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.derlys.config.DatabaseConfig;
import com.derlys.db.DatabaseConnection;
import com.derlys.model.Usuario;
import com.derlys.repository.UsuarioRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws SQLException, IOException {
        DatabaseConfig config = DatabaseConfig.load();
        DatabaseConnection connection = new DatabaseConnection(config);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> src == null ? JsonNull.INSTANCE
                                : new JsonPrimitive(src.toString()))
                .create();
        try (var conn = connection.connect()) {
            UsuarioRepository usuarioRepository = new UsuarioRepository(conn);
            List<Usuario> usuarios = usuarioRepository.findAll();
            System.out.println(gson.toJson(usuarios));
        }
    }
}
