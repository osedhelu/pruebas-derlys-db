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

    private final Connection connection;

    public UsuarioRepository(Connection connection) {
        this.connection = connection;
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
        var fecha = rs.getTimestamp("fecha_creacion").toLocalDateTime();
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("rol_id"),
                fecha);
    }
}
