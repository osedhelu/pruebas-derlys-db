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

    public Usuario crearCliente(
            String nombre, String username, String email, String telefono, String password) {
        String userNorm = normalizarUsername(username);
        if (existeUsername(userNorm, null)) {
            throw new RuntimeException("Ya existe un usuario con el nombre de usuario: " + userNorm);
        }
        if (email != null && !email.isBlank() && existeEmail(email.trim(), null)) {
            throw new RuntimeException("Ya existe un usuario con el email: " + email.trim());
        }
        String sql =
                """
                INSERT INTO usuarios (nombre, username, email, telefono, password_hash, rol_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, userNorm);
            statement.setString(3, email == null || email.isBlank() ? null : email.trim());
            statement.setString(4, telefono == null || telefono.isBlank() ? null : telefono.trim());
            statement.setString(5, password);
            statement.setInt(6, ROL_CLIENTE);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    Usuario creado = buscarPorId(keys.getInt(1));
                    if (creado != null) {
                        return creado;
                    }
                }
            }
            return buscarClientePorUsername(userNorm);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear cliente: " + e.getMessage(), e);
        }
    }

    private Usuario buscarClientePorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND rol_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, ROL_CLIENTE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            throw new RuntimeException("No se pudo crear el cliente");
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por usuario", e);
        }
    }

    private static String normalizarUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private boolean existeUsername(String username, Integer excluirId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String sql = excluirId == null
                ? "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1"
                : "SELECT 1 FROM usuarios WHERE username = ? AND id != ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            if (excluirId != null) {
                statement.setInt(2, excluirId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar nombre de usuario", e);
        }
    }

    private boolean existeEmail(String email, Integer excluirId) {
        String sql = excluirId == null
                ? "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1"
                : "SELECT 1 FROM usuarios WHERE email = ? AND id != ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            if (excluirId != null) {
                statement.setInt(2, excluirId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar email", e);
        }
    }

    public void actualizarCliente(
            int id,
            String nombre,
            String username,
            String email,
            String telefono,
            String passwordNuevo) {
        String userNorm = normalizarUsername(username);
        if (existeUsername(userNorm, id)) {
            throw new RuntimeException("Ya existe otro usuario con el nombre de usuario: " + userNorm);
        }
        String emailTrim = email == null || email.isBlank() ? null : email.trim();
        if (emailTrim != null && existeEmail(emailTrim, id)) {
            throw new RuntimeException("Ya existe otro usuario con el email: " + emailTrim);
        }
        String telTrim = telefono == null || telefono.isBlank() ? null : telefono.trim();

        String sql = passwordNuevo == null || passwordNuevo.isBlank()
                ? """
                UPDATE usuarios SET nombre = ?, username = ?, email = ?, telefono = ?
                WHERE id = ? AND rol_id = ?
                """
                : """
                UPDATE usuarios SET nombre = ?, username = ?, email = ?, telefono = ?, password_hash = ?
                WHERE id = ? AND rol_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, userNorm);
            statement.setString(3, emailTrim);
            statement.setString(4, telTrim);
            if (passwordNuevo == null || passwordNuevo.isBlank()) {
                statement.setInt(5, id);
                statement.setInt(6, ROL_CLIENTE);
            } else {
                statement.setString(5, passwordNuevo);
                statement.setInt(6, id);
                statement.setInt(7, ROL_CLIENTE);
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

    public Usuario buscar(String username) throws SQLException {
        String userNorm = normalizarUsername(username);
        String sql = "SELECT * FROM usuarios WHERE username = ? OR email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userNorm);
            statement.setString(2, username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
            return null;
        }
    }

    public Usuario createUsuario(
            Integer id,
            String nombre,
            String username,
            String email,
            String telefono,
            String passwordHash,
            Integer rolId,
            LocalDateTime fechaCreacion)
            throws SQLException {
        String sql =
                """
                INSERT INTO usuarios (id, nombre, username, email, telefono, password_hash, rol_id, fecha_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, nombre);
            statement.setString(3, normalizarUsername(username));
            statement.setString(4, email);
            statement.setString(5, telefono);
            statement.setString(6, passwordHash);
            statement.setInt(7, rolId);
            statement.setTimestamp(8, Timestamp.valueOf(fechaCreacion));

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el usuario", e);
        }
        return new Usuario(
                id,
                nombre,
                normalizarUsername(username),
                email,
                telefono,
                passwordHash,
                rolId,
                fechaCreacion);
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        var ts = rs.getTimestamp("fecha_creacion");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getString("password_hash"),
                rs.getInt("rol_id"),
                fecha);
    }
}
