package com.derlys.repository;

import com.derlys.model.PreventaDetalle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PreventaRepository {

    private final Connection connection;

    public PreventaRepository(Connection connection) {
        this.connection = connection;
    }

    public List<PreventaDetalle> listarDetalle() {
        String sql = """
                SELECT p.id, u.nombre AS cliente_nombre, l.codigo_lote, p.cantidad_apartada, p.fecha_apartado, p.estado
                FROM preventas p
                INNER JOIN usuarios u ON u.id = p.cliente_id
                INNER JOIN lotes l ON l.id = p.lote_id
                ORDER BY p.fecha_apartado DESC
                """;
        List<PreventaDetalle> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                var ts = rs.getTimestamp("fecha_apartado");
                LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
                lista.add(new PreventaDetalle(
                        rs.getInt("id"),
                        rs.getString("cliente_nombre"),
                        rs.getString("codigo_lote"),
                        rs.getInt("cantidad_apartada"),
                        fecha,
                        rs.getString("estado")));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preventas", e);
        }
    }

    public void crear(int clienteId, int loteId, int cantidad) {
        String sql = """
                INSERT INTO preventas (cliente_id, lote_id, cantidad_apartada, estado)
                VALUES (?, ?, ?, 'pendiente')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clienteId);
            statement.setInt(2, loteId);
            statement.setInt(3, cantidad);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la preventa", e);
        }
    }
}
