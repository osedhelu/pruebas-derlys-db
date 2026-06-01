package com.derlys.repository;

import com.derlys.model.Preventa;
import com.derlys.model.PreventaDetalle;
import com.derlys.model.PreventaEstados;
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
                SELECT p.id, u.nombre AS cliente_nombre, u.telefono AS cliente_telefono,
                       l.codigo_lote, p.cantidad_apartada, p.fecha_apartado, p.estado
                FROM preventas p
                INNER JOIN usuarios u ON u.id = p.cliente_id
                INNER JOIN lotes l ON l.id = p.lote_id
                ORDER BY p.fecha_apartado DESC
                """;
        List<PreventaDetalle> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                lista.add(mapDetalle(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preventas", e);
        }
    }

    public Preventa buscarPorId(int id) {
        String sql = "SELECT * FROM preventas WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapPreventa(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar preventa", e);
        }
    }

    public void crear(int clienteId, int loteId, int cantidad) {
        String sql = """
                INSERT INTO preventas (cliente_id, lote_id, cantidad_apartada, estado)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clienteId);
            statement.setInt(2, loteId);
            statement.setInt(3, cantidad);
            statement.setString(4, PreventaEstados.PENDIENTE);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la preventa", e);
        }
    }

    public void cambiarEstado(int preventaId, String nuevoEstado) {
        String estadoNorm = nuevoEstado == null ? "" : nuevoEstado.trim().toLowerCase();
        if (!PreventaEstados.MANUALES.contains(estadoNorm)) {
            throw new RuntimeException(
                    "Estado no válido. Use: pendiente, listo, mora o entregado. (Cobrada = pestaña Cobrar)");
        }

        Preventa preventa = buscarPorId(preventaId);
        if (preventa == null) {
            throw new RuntimeException("Preventa no encontrada");
        }
        String actual = preventa.estado() == null ? "" : preventa.estado().toLowerCase();

        if (PreventaEstados.estaCobrada(actual)) {
            if (!PreventaEstados.ENTREGADO.equals(estadoNorm)) {
                throw new RuntimeException(
                        "La preventa ya está cobrada. Solo puede confirmar «entregado» si aún no lo estaba.");
            }
        } else if (PreventaEstados.COMPLETADA.equals(estadoNorm)) {
            throw new RuntimeException("Para registrar el cobro use la pestaña «Cobrar preventa».");
        }

        try {
            actualizarEstado(preventaId, estadoNorm);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado: " + e.getMessage(), e);
        }
    }

    public void completarPreventa(int preventaId, int usuarioVendedorId, double monto, String descripcion) {
        Preventa preventa = buscarPorId(preventaId);
        if (preventa == null) {
            throw new RuntimeException("Preventa no encontrada");
        }
        if (!PreventaEstados.puedeCobrarse(preventa.estado())) {
            throw new RuntimeException(
                    "Solo se pueden cobrar preventas que aún no están pagadas (estado actual: "
                            + PreventaEstados.etiqueta(preventa.estado())
                            + ")");
        }
        if (monto <= 0) {
            throw new RuntimeException("El monto de la venta debe ser mayor que 0");
        }

        String desc = (descripcion == null || descripcion.isBlank())
                ? "Venta por preventa #" + preventaId + " — " + preventa.cantidadApartada() + " pollos"
                : descripcion.trim();

        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            new TransaccionRepository(connection).insertarTransaccion(
                    preventa.loteId(),
                    usuarioVendedorId,
                    "VENTA",
                    1,
                    preventa.cantidadApartada(),
                    desc,
                    monto);

            actualizarEstado(preventaId, PreventaEstados.COMPLETADA);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // sin acción
            }
            throw new RuntimeException("Error al completar la preventa: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException ignored) {
                // sin acción
            }
        }
    }

    private void actualizarEstado(int preventaId, String estado) throws SQLException {
        String sql = "UPDATE preventas SET estado = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado);
            statement.setInt(2, preventaId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No se actualizó la preventa");
            }
        }
    }

    private PreventaDetalle mapDetalle(ResultSet rs) throws SQLException {
        var ts = rs.getTimestamp("fecha_apartado");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
        return new PreventaDetalle(
                rs.getInt("id"),
                rs.getString("cliente_nombre"),
                rs.getString("cliente_telefono"),
                rs.getString("codigo_lote"),
                rs.getInt("cantidad_apartada"),
                fecha,
                rs.getString("estado"));
    }

    private Preventa mapPreventa(ResultSet rs) throws SQLException {
        var ts = rs.getTimestamp("fecha_apartado");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
        return new Preventa(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getInt("lote_id"),
                rs.getInt("cantidad_apartada"),
                fecha,
                rs.getString("estado"));
    }
}
