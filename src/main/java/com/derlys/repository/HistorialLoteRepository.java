package com.derlys.repository;

import com.derlys.model.MovimientoLote;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistorialLoteRepository {

    private static final String SQL_TRANSACCIONES = """
            SELECT
                t.fecha,
                tm.nombre AS tipo,
                t.descripcion,
                t.cantidad_unidades,
                u.nombre AS responsable,
                CASE
                    WHEN tm.nombre = 'VENTA' THEN (
                        SELECT IFNULL(SUM(ac.haber), 0)
                        FROM asientos_contables ac
                        WHERE ac.transaccion_id = t.id AND ac.cuenta_id = 4
                    )
                    WHEN tm.nombre IN ('GASTO_OPERATIVO', 'INVERSION_ACTIVO') THEN (
                        SELECT IFNULL(SUM(ac.debe), 0)
                        FROM asientos_contables ac
                        WHERE ac.transaccion_id = t.id AND ac.cuenta_id IN (3, 5)
                    )
                    WHEN tm.nombre = 'COMPRA_LOTE' THEN (
                        SELECT IFNULL(SUM(ac.debe), 0)
                        FROM asientos_contables ac
                        WHERE ac.transaccion_id = t.id AND ac.cuenta_id = 7
                    )
                    WHEN tm.nombre = 'APORTE_CAPITAL' THEN (
                        SELECT IFNULL(SUM(ac.debe), 0)
                        FROM asientos_contables ac
                        WHERE ac.transaccion_id = t.id AND ac.cuenta_id = 1
                    )
                    ELSE 0
                END AS monto
            FROM transacciones t
            INNER JOIN tipos_movimiento tm ON tm.id = t.tipo_movimiento_id
            INNER JOIN usuarios u ON u.id = t.usuario_id
            WHERE t.lote_id = ?
            """;

    private static final String SQL_PREVENTAS = """
            SELECT
                p.fecha_apartado AS fecha,
                p.cantidad_apartada,
                p.estado,
                u.nombre AS cliente
            FROM preventas p
            INNER JOIN usuarios u ON u.id = p.cliente_id
            WHERE p.lote_id = ?
            """;

    private final Connection connection;

    public HistorialLoteRepository(Connection connection) {
        this.connection = connection;
    }

    public List<MovimientoLote> listarHistorial(int loteId) {
        List<MovimientoLote> movimientos = new ArrayList<>();
        movimientos.addAll(listarTransacciones(loteId));
        movimientos.addAll(listarPreventas(loteId));
        movimientos.sort(Comparator.comparing(MovimientoLote::fecha, Comparator.nullsLast(Comparator.reverseOrder())));
        return movimientos;
    }

    private List<MovimientoLote> listarTransacciones(int loteId) {
        List<MovimientoLote> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_TRANSACCIONES)) {
            statement.setInt(1, loteId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    double monto = rs.getDouble("monto");
                    Double montoVal = rs.wasNull() || monto <= 0 ? null : monto;
                    lista.add(new MovimientoLote(
                            leerFecha(rs, "fecha"),
                            rs.getString("tipo"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad_unidades"),
                            montoVal,
                            rs.getString("responsable"),
                            null));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar transacciones del historial", e);
        }
        return lista;
    }

    private List<MovimientoLote> listarPreventas(int loteId) {
        List<MovimientoLote> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_PREVENTAS)) {
            statement.setInt(1, loteId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int cantidad = rs.getInt("cantidad_apartada");
                    String cliente = rs.getString("cliente");
                    String estado = rs.getString("estado");
                    String tipo = switch (estado == null ? "" : estado.toLowerCase()) {
                        case "completada" -> "PREVENTA COBRADA";
                        case "entregado" -> "PREVENTA ENTREGADA";
                        case "listo" -> "PREVENTA LISTA";
                        case "mora" -> "PREVENTA EN MORA";
                        default -> "PREVENTA";
                    };
                    lista.add(new MovimientoLote(
                            leerFecha(rs, "fecha"),
                            tipo,
                            "Apartado de pollos — cliente: " + cliente,
                            cantidad,
                            null,
                            cliente,
                            estado));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preventas del historial", e);
        }
        return lista;
    }

    private static LocalDateTime leerFecha(ResultSet rs, String columna) throws SQLException {
        var ts = rs.getTimestamp(columna);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
