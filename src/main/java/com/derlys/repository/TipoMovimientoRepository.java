package com.derlys.repository;

import com.derlys.model.TipoMovimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoMovimientoRepository {

    private final Connection connection;

    public TipoMovimientoRepository(Connection connection) {
        this.connection = connection;
    }

    public List<TipoMovimiento> listar() {
        String sql = "SELECT id, nombre, descripcion FROM tipos_movimiento ORDER BY id";
        List<TipoMovimiento> tipos = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                tipos.add(new TipoMovimiento(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")));
            }
            return tipos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar tipos de movimiento", e);
        }
    }
}
