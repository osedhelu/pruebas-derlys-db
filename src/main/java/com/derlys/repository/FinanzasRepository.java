package com.derlys.repository;

import com.derlys.model.LoteFinanciero;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FinanzasRepository {

    private static final String SQL_REPORTE_FINANCIERO = """
            SELECT
                l.id,
                l.codigo_lote,
                COALESCE((
                    SELECT SUM(ac.haber)
                    FROM transacciones t
                    INNER JOIN asientos_contables ac ON ac.transaccion_id = t.id
                    INNER JOIN plan_cuentas pc ON pc.id = ac.cuenta_id
                    WHERE t.lote_id = l.id AND pc.naturaleza = 'credito' AND ac.haber > 0
                ), 0) AS ingresos,
                COALESCE(l.costo_inicial, 0) + COALESCE((
                    SELECT SUM(ac.debe)
                    FROM transacciones t
                    INNER JOIN asientos_contables ac ON ac.transaccion_id = t.id
                    INNER JOIN plan_cuentas pc ON pc.id = ac.cuenta_id
                    WHERE t.lote_id = l.id AND pc.codigo IN ('5105', '5110', '1524')
                ), 0) AS gastos,
                COALESCE((
                    SELECT SUM(t.cantidad_unidades)
                    FROM transacciones t
                    INNER JOIN tipos_movimiento tm ON tm.id = t.tipo_movimiento_id
                    WHERE t.lote_id = l.id AND tm.nombre = 'VENTA'
                ), 0) AS pollos_vendidos
            FROM lotes l
            ORDER BY l.id ASC
            """;

    private final Connection connection;

    public FinanzasRepository(Connection connection) {
        this.connection = connection;
    }

    public List<LoteFinanciero> listarRentabilidadPorLote() {
        List<LoteFinanciero> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_REPORTE_FINANCIERO);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                double ingresos = rs.getDouble("ingresos");
                double gastos = rs.getDouble("gastos");
                double resultado = ingresos - gastos;
                lista.add(new LoteFinanciero(
                        rs.getInt("id"),
                        rs.getString("codigo_lote"),
                        ingresos,
                        gastos,
                        resultado,
                        rs.getInt("pollos_vendidos"),
                        calcularEstado(ingresos, gastos, resultado)));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al generar reporte financiero", e);
        }
    }

    private static String calcularEstado(double ingresos, double gastos, double resultado) {
        if (ingresos == 0 && gastos == 0) {
            return "SIN MOVIMIENTOS";
        }
        if (resultado < 0) {
            return "PÉRDIDA";
        }
        if (resultado > 0) {
            return "GANANCIA";
        }
        return "EQUILIBRIO";
    }
}
