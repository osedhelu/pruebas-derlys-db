package com.derlys.repository;

import com.derlys.model.Transaccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransaccionRepository {

    private final Connection connection;

    public TransaccionRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Transaccion> Listar() {
        String sql = "SELECT * FROM transacciones l";
        List<Transaccion> transacciones = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery();) {
            while (resultSet.next()) {
                transacciones.add(mapRow(resultSet));
            }
            return transacciones;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las transaciones", e);
        }

    }

    private Transaccion mapRow(ResultSet rs) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp("fecha");
        LocalDateTime fecha = (timestamp != null) ? timestamp.toLocalDateTime() : null;
        return new Transaccion(
                rs.getInt("id"),
                fecha,
                rs.getString("descripcion"),
                rs.getInt("lote_id"),
                rs.getInt("usuario_id"),
                rs.getInt("tipo_movimiento_id"),
                rs.getInt("cantidad_unidades"),
                rs.getString("comprobante_url")
        );
    }

}
