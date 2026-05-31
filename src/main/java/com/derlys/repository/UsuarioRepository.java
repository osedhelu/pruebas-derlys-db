package com.derlys.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.derlys.model.Usuario;

public class UsuarioRepository {

    public static final int ROL_CLIENTE = 4;

    private final Connection connection;

    public UsuarioRepository(Connection connection) {
        this.connection = connection;
    }

    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ? AND rol_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, ROL_CLIENTE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }

    public Usuario crearCliente(String nombre, String email, String password) {
        String sql = "INSERT INTO usuarios (nombre, email, password_hash, rol_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setInt(4, ROL_CLIENTE);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return buscarPorId(keys.getInt(1));
                }
            }
            throw new RuntimeException("No se pudo crear el cliente");
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear cliente: " + e.getMessage(), e);
        }
    }

    public void actualizarCliente(int id, String nombre, String email, String passwordNuevo) {
        String sql = passwordNuevo == null || passwordNuevo.isBlank()
                ? "UPDATE usuarios SET nombre = ?, email = ? WHERE id = ? AND rol_id = ?"
                : "UPDATE usuarios SET nombre = ?, email = ?, password_hash = ? WHERE id = ? AND rol_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            if (passwordNuevo == null || passwordNuevo.isBlank()) {
                statement.setInt(3, id);
                statement.setInt(4, ROL_CLIENTE);
            } else {
                statement.setString(3, passwordNuevo);
                statement.setInt(4, id);
                statement.setInt(5, ROL_CLIENTE);
            }
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Cliente no encontrado");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
    }

    public void eliminarCliente(int id) {
        if (tienePreventas(id)) {
            throw new RuntimeException("No se puede eliminar: el cliente tiene preventas registradas.");
        }
        String sql = "DELETE FROM usuarios WHERE id = ? AND rol_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, ROL_CLIENTE);
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Cliente no encontrado");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    private boolean tienePreventas(int clienteId) {
        String sql = "SELECT COUNT(*) FROM preventas WHERE cliente_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clienteId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar preventas del cliente", e);
        }
    }

    public List<Usuario> listarClientes() {
        String sql = "SELECT * FROM usuarios WHERE rol_id = 4 ORDER BY nombre ASC";
        List<Usuario> clientes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clientes.add(mapRow(resultSet));
            }
            return clientes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes", e);
        }
    }

    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY id ASC";
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                usuarios.add(mapRow(resultSet));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener los usuarios", e);
        }
    }

    public Usuario buscar(String emailONombre) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email like ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailONombre + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
            return null;
        }
    }

    public Usuario createUsuario(Integer id, String nombre, String email, String passwordHash, Integer rolId,
            LocalDateTime fechaCreacion) throws SQLException {
        String sql = "INSERT INTO usuarios (id, nombre, email, password_hash, rol_id, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, nombre);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            statement.setInt(5, rolId);
            statement.setTimestamp(6, Timestamp.valueOf(fechaCreacion));

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el usuario", e);
        }
        return new Usuario(id, nombre, email, passwordHash, rolId, fechaCreacion);
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        var ts = rs.getTimestamp("fecha_creacion");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("rol_id"),
                fecha);
    }
}
