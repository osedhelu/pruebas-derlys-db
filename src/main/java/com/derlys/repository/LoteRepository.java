package com.derlys.repository;

import com.derlys.model.Lote;
import com.derlys.model.LoteReporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoteRepository {

    private static final int DIAS_META_SACRIFICIO = 45;
    private static final String SQL_REPORTE_DETALLADO = """
            SELECT
                l.id,
                l.codigo_lote,
                l.cantidad_inicial,
                l.fecha_entrada,
                CAST(julianday('now') - julianday(l.fecha_entrada) AS INTEGER) AS dias_vida,
                (? - CAST(julianday('now') - julianday(l.fecha_entrada) AS INTEGER)) AS dias_para_sacrificio,
                (SELECT IFNULL(SUM(cantidad_unidades), 0) FROM transacciones WHERE lote_id = l.id) AS total_salidas,
                (SELECT IFNULL(SUM(cantidad_apartada), 0) FROM preventas WHERE lote_id = l.id AND estado IN ('pendiente', 'listo', 'mora')) AS total_apartado,
                (l.cantidad_inicial
                    - (SELECT IFNULL(SUM(cantidad_unidades), 0) FROM transacciones WHERE lote_id = l.id)
                    - (SELECT IFNULL(SUM(cantidad_apartada), 0) FROM preventas WHERE lote_id = l.id AND estado IN ('pendiente', 'listo', 'mora'))
                ) AS disponible_venta
            FROM lotes l
            """;

    private final Connection connection;

    public LoteRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Lote> listar() {
        String sql = "SELECT * FROM lotes";
        List<Lote> lote = new ArrayList();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                lote.add(mapRow(resultSet));
            }
            return lote;

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los lotes", e);
        }
    }

    public List<LoteReporte> listarReporteDetallado() {
        String sql = SQL_REPORTE_DETALLADO + " ORDER BY l.id ASC";
        return ejecutarReporte(sql, null);
    }

    public LoteReporte obtenerReporteDetallado(int loteId) {
        String sql = SQL_REPORTE_DETALLADO + " WHERE l.id = ?";
        List<LoteReporte> filas = ejecutarReporte(sql, loteId);
        return filas.isEmpty() ? null : filas.get(0);
    }

    private List<LoteReporte> ejecutarReporte(String sql, Integer loteId) {
        List<LoteReporte> reportes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, DIAS_META_SACRIFICIO);
            if (loteId != null) {
                statement.setInt(2, loteId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapReporteRow(rs));
                }
            }
            return reportes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al generar reporte de lotes", e);
        }
    }

    private LoteReporte mapReporteRow(ResultSet rs) throws SQLException {
        String fechaStr = rs.getString("fecha_entrada");
        LocalDate fecha = fechaStr != null ? LocalDate.parse(fechaStr) : null;
        return new LoteReporte(
                rs.getInt("id"),
                rs.getString("codigo_lote"),
                rs.getInt("cantidad_inicial"),
                fecha,
                rs.getInt("dias_vida"),
                rs.getInt("dias_para_sacrificio"),
                rs.getInt("total_salidas"),
                rs.getInt("total_apartado"),
                rs.getInt("disponible_venta"));
    }

    public Lote obtenerUnLote(Integer id) {
        String sql = "SELECT * FROM lotes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar este lote con ID: " + id, e);
        }
    }

    public Lote crearLote(int cantidad, String raza) {
        String sql = "INSERT INTO lotes (codigo_lote, fecha_entrada, cantidad_inicial, raza, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCount = "SELECT COUNT(*) + 1 FROM lotes";

        try {
            int siguienteNumero = 1;
            try (PreparedStatement stmtCount = connection.prepareStatement(sqlCount);
                    ResultSet rs = stmtCount.executeQuery()) {
                if (rs.next()) {
                    siguienteNumero = rs.getInt(1);
                }
            }

            String codigoLote = "LT-" + String.format("%03d", siguienteNumero);
            LocalDate fechaActual = LocalDate.now();
            String razaFinal = (raza == null || raza.isBlank()) ? "Ross 308" : raza.trim();
            String estadoInicial = "activo";
            String observaciones = "Lote creado desde la app.";

            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, codigoLote);
                stmt.setString(2, fechaActual.toString());
                stmt.setInt(3, cantidad);
                stmt.setString(4, razaFinal);
                stmt.setString(5, estadoInicial);
                stmt.setString(6, observaciones);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Lote creado = obtenerUnLote(keys.getInt(1));
                        if (creado != null) {
                            return creado;
                        }
                    }
                }
            }

            return buscarPorCodigo(codigoLote);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el lote: " + e.getMessage(), e);
        }
    }

    private Lote buscarPorCodigo(String codigoLote) throws SQLException {
        String sql = "SELECT * FROM lotes WHERE codigo_lote = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigoLote);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    }
    private Lote mapRow(ResultSet rs) throws SQLException {
        String fechaStr = rs.getString("fecha_entrada");
        LocalDate fecha = (fechaStr != null) ? LocalDate.parse(fechaStr) : null;
        return new Lote(
                rs.getInt("id"),
                rs.getString("codigo_lote"),
                fecha,
                rs.getInt("cantidad_inicial"),
                rs.getString("raza"),
                rs.getString("estado"),
                rs.getString("observaciones")
        );
    }

}
