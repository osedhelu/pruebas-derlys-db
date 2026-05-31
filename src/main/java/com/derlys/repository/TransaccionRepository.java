package com.derlys.repository;

import com.derlys.model.Transaccion;
import com.derlys.model.TransaccionDetalle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransaccionRepository {

    private final Connection connection;

    public TransaccionRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Transaccion> listar() {
        String sql = "SELECT * FROM transacciones ORDER BY fecha DESC";
        return ejecutarLista(sql, null);
    }

    public List<TransaccionDetalle> listarPorLote(int loteId) {
        String sql = """
                SELECT t.id, t.fecha, tm.nombre AS tipo_nombre, t.cantidad_unidades, t.descripcion
                FROM transacciones t
                INNER JOIN tipos_movimiento tm ON tm.id = t.tipo_movimiento_id
                WHERE t.lote_id = ?
                ORDER BY t.fecha DESC
                """;
        List<TransaccionDetalle> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loteId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapDetalle(rs, false));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar transacciones del lote", e);
        }
    }

    public List<TransaccionDetalle> listarComprasPorLote(int loteId) {
        String sql = """
                SELECT t.id, t.fecha, tm.nombre AS tipo_nombre, t.cantidad_unidades, t.descripcion,
                    (SELECT COALESCE(SUM(ac.debe), 0)
                     FROM asientos_contables ac
                     WHERE ac.transaccion_id = t.id AND ac.debe > 0
                       AND ac.cuenta_id IN (3, 5)) AS monto_ref
                FROM transacciones t
                INNER JOIN tipos_movimiento tm ON tm.id = t.tipo_movimiento_id
                WHERE t.lote_id = ? AND tm.nombre IN ('GASTO_OPERATIVO', 'INVERSION_ACTIVO')
                ORDER BY t.fecha DESC
                """;
        List<TransaccionDetalle> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loteId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapDetalle(rs, true));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar compras del lote", e);
        }
    }

    private static final int CUENTA_CAJA = 1;
    private static final int CUENTA_GASTO_MEDICINAS = 3;
    private static final int CUENTA_INGRESOS_VENTAS = 4;
    private static final int CUENTA_EQUIPOS = 5;

    public void crear(
            int loteId, int usuarioId, String tipoNombre, int tipoMovimientoId, int cantidad, String descripcion,
            Double monto) {
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            insertarTransaccion(loteId, usuarioId, tipoNombre, tipoMovimientoId, cantidad, descripcion, monto);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // sin acción
            }
            throw new RuntimeException("Error al registrar la transacción", e);
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException ignored) {
                // sin acción
            }
        }
    }

    void insertarTransaccion(
            int loteId, int usuarioId, String tipoNombre, int tipoMovimientoId, int cantidad, String descripcion,
            Double monto) throws SQLException {
        String sql = """
                INSERT INTO transacciones (descripcion, lote_id, usuario_id, tipo_movimiento_id, cantidad_unidades)
                VALUES (?, ?, ?, ?, ?)
                """;
        int transaccionId;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, descripcion);
            statement.setInt(2, loteId);
            statement.setInt(3, usuarioId);
            statement.setInt(4, tipoMovimientoId);
            statement.setInt(5, cantidad);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID de la transacción");
                }
                transaccionId = keys.getInt(1);
            }
        }

        if (monto != null && monto > 0) {
            registrarAsientos(transaccionId, tipoNombre, monto);
        }
    }

    private void registrarAsientos(int transaccionId, String tipoNombre, double monto) throws SQLException {
        switch (tipoNombre) {
            case "VENTA" -> {
                insertarAsiento(transaccionId, CUENTA_CAJA, monto, 0);
                insertarAsiento(transaccionId, CUENTA_INGRESOS_VENTAS, 0, monto);
            }
            case "GASTO_OPERATIVO" -> {
                insertarAsiento(transaccionId, CUENTA_GASTO_MEDICINAS, monto, 0);
                insertarAsiento(transaccionId, CUENTA_CAJA, 0, monto);
            }
            case "INVERSION_ACTIVO" -> {
                insertarAsiento(transaccionId, CUENTA_EQUIPOS, monto, 0);
                insertarAsiento(transaccionId, CUENTA_CAJA, 0, monto);
            }
            default -> { }
        }
    }

    private void insertarAsiento(int transaccionId, int cuentaId, double debe, double haber) throws SQLException {
        String sql = "INSERT INTO asientos_contables (transaccion_id, cuenta_id, debe, haber) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transaccionId);
            statement.setInt(2, cuentaId);
            statement.setDouble(3, debe);
            statement.setDouble(4, haber);
            statement.executeUpdate();
        }
    }

    private List<Transaccion> ejecutarLista(String sql, Integer loteId) {
        List<Transaccion> transacciones = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (loteId != null) {
                statement.setInt(1, loteId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transacciones.add(mapRow(resultSet));
                }
            }
            return transacciones;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las transacciones", e);
        }
    }

    private TransaccionDetalle mapDetalle(ResultSet rs, boolean conMonto) throws SQLException {
        var timestamp = rs.getTimestamp("fecha");
        LocalDateTime fecha = timestamp != null ? timestamp.toLocalDateTime() : null;
        Double monto = null;
        if (conMonto) {
            double valor = rs.getDouble("monto_ref");
            if (!rs.wasNull() && valor > 0) {
                monto = valor;
            }
        }
        return new TransaccionDetalle(
                rs.getInt("id"),
                fecha,
                rs.getString("tipo_nombre"),
                rs.getInt("cantidad_unidades"),
                rs.getString("descripcion"),
                monto);
    }

    private Transaccion mapRow(ResultSet rs) throws SQLException {
        var timestamp = rs.getTimestamp("fecha");
        LocalDateTime fecha = timestamp != null ? timestamp.toLocalDateTime() : null;
        return new Transaccion(
                rs.getInt("id"),
                fecha,
                rs.getString("descripcion"),
                rs.getInt("lote_id"),
                rs.getInt("usuario_id"),
                rs.getInt("tipo_movimiento_id"),
                rs.getInt("cantidad_unidades"),
                rs.getString("comprobante_url"));
    }
}
