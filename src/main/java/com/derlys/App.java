package com.derlys;

import java.io.IOException;
import java.sql.SQLException;
import com.derlys.config.DatabaseConfig;
import com.derlys.db.DatabaseConnection;
import com.derlys.model.Lote;
import com.derlys.repository.LoteRepository;
import java.util.List;



public class App {
    public static void main(String[] args) throws SQLException, IOException {
        DatabaseConfig config = DatabaseConfig.load();
        DatabaseConnection connection = new DatabaseConnection(config);
       
        try (var conn = connection.connect()) {
//            UsuarioRepository usuarioRepository = new UsuarioRepository(conn);
            LoteRepository loteRepository = new LoteRepository(conn);
//           List<Usuario> usuarios = usuarioRepository.findAll();
            List<Lote> lotes = loteRepository.listar();
            Lote.printAll(lotes);
//           if(lote != null) {
//               lote.print();
//            }else {
//               System.out.println("este lote no existe es invalido");
//           }     
        }
    }
}
