package com.derlys.repository;

import com.derlys.model.CuentaSaldo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContabilidadRepository {

    private static final String SQL_SALDOS_BASE = """
            SELECT
                pc.codigo,
                pc.nombre AS cuenta,
                pc.naturaleza,
                SUM(ac.debe) AS total_debe,
                SUM(ac.haber) AS total_haber,
                (SUM(ac.debe) - SUM(ac.haber)) AS saldo
            FROM asientos_contables ac
            INNER JOIN plan_cuentas pc ON ac.cuenta_id = pc.id
            INNER JOIN transacciones t ON ac.transaccion_id = t.id
            """;

    private final Connection connection;

    public ContabilidadRepository(Connection connection) {
        this.connection = connection;
    }

    public List<CuentaSaldo> listarSaldosPorCuenta(Integer loteId) {
        String sql = SQL_SALDOS_BASE
                + (loteId != null ? " WHERE t.lote_id = ? " : " ")
                + " GROUP BY pc.id, pc.codigo, pc.nombre, pc.naturaleza ORDER BY pc.codigo ASC";

        List<CuentaSaldo> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (loteId != null) {
                statement.setInt(1, loteId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CuentaSaldo(
                            rs.getString("codigo"),
                            rs.getString("cuenta"),
                            rs.getString("naturaleza"),
                            rs.getDouble("total_debe"),
                            rs.getDouble("total_haber"),
                            rs.getDouble("saldo")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar saldos contables", e);
        }
    }
}
