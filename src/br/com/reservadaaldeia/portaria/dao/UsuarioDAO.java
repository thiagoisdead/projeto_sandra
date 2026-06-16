package br.com.reservadaaldeia.portaria.dao;

import br.com.reservadaaldeia.portaria.database.DatabaseHelper;
import br.com.reservadaaldeia.portaria.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import br.com.reservadaaldeia.portaria.exception.DAOException;

public class UsuarioDAO {
    
    public Usuario autenticar(String login, String senha) throws DAOException {
        String sql = "SELECT * FROM usuarios WHERE login = ? AND senha = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, login);
            stmt.setString(2, senha);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getString("id"));
                    u.setLogin(rs.getString("login"));
                    u.setSenha(rs.getString("senha"));
                    u.setTipo(rs.getString("tipo"));
                    u.setCpf(rs.getString("cpf"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro na autenticacao: " + e.getMessage());
            throw new DAOException("Erro na autenticação de usuário.", e);
        }
        return null;
    }

    public java.util.List<Usuario> listarTodos() throws DAOException {
        java.util.List<Usuario> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY login";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getString("id"));
                u.setLogin(rs.getString("login"));
                u.setSenha(rs.getString("senha"));
                u.setTipo(rs.getString("tipo"));
                u.setCpf(rs.getString("cpf"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar usuários.", e);
        }
        return lista;
    }

    public boolean inserir(Usuario u) throws DAOException {
        String sql = "INSERT INTO usuarios (id, login, senha, tipo, cpf) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            stmt.setString(1, uuid);
            stmt.setString(2, u.getLogin());
            stmt.setString(3, u.getSenha());
            stmt.setString(4, u.getTipo());
            String cleanCpf = u.getCpf() != null ? u.getCpf().replaceAll("[^0-9]", "") : "";
            stmt.setString(5, cleanCpf);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao inserir usuário.", e);
        }
    }

    public boolean existeLogin(String login) throws DAOException {
        String sql = "SELECT 1 FROM usuarios WHERE login = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao verificar login.", e);
        }
    }

    public void deletar(String id) throws DAOException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao excluir usuário.", e);
        }
    }

    public boolean existeCpf(String cpf) throws DAOException {
        String sql = "SELECT 1 FROM usuarios WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String cleanCpf = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";
            stmt.setString(1, cleanCpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Se houver próximo, existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao verificar CPF de usuário.", e);
        }
    }

    public void atualizar(Usuario u) throws DAOException {
        String sql = "UPDATE usuarios SET login = ?, senha = ?, tipo = ?, cpf = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getLogin());
            stmt.setString(2, u.getSenha());
            stmt.setString(3, u.getTipo());
            
            String cleanCpf = u.getCpf() != null ? u.getCpf().replaceAll("[^0-9]", "") : "";
            stmt.setString(4, cleanCpf);
            stmt.setString(5, u.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao atualizar usuário.", e);
        }
    }

}
