package br.com.reservadaaldeia.portaria.dao;

import br.com.reservadaaldeia.portaria.database.DatabaseHelper;
import br.com.reservadaaldeia.portaria.model.Morador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.reservadaaldeia.portaria.exception.DAOException;

public class MoradorDAO {

    public void inserir(Morador morador) throws DAOException {
        String sql = "INSERT INTO moradores(nome, cpf, login, senha, apartamento_id) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, morador.getNome());
            String cleanCpf = morador.getCpf() != null ? morador.getCpf().replaceAll("[^0-9]", "") : "";
            stmt.setString(2, cleanCpf);
            stmt.setString(3, morador.getLogin());
            stmt.setString(4, morador.getSenha());
            stmt.setInt(5, morador.getApartamentoId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao cadastrar morador.", e);
        }
    }

    public List<Morador> listarTodos() throws DAOException {
        List<Morador> lista = new ArrayList<>();
        String sql = "SELECT * FROM moradores ORDER BY nome";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Morador m = new Morador();
                m.setId(rs.getInt("id"));
                m.setNome(rs.getString("nome"));
                m.setCpf(rs.getString("cpf"));
                m.setLogin(rs.getString("login"));
                m.setSenha(rs.getString("senha"));
                m.setApartamentoId(rs.getInt("apartamento_id"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar moradores.", e);
        }
        return lista;
    }

    public List<Morador> buscarPorNomeOuCpf(String busca) throws DAOException {
        List<Morador> lista = new ArrayList<>();
        String sql = "SELECT * FROM moradores WHERE nome LIKE ? OR replace(replace(cpf, '.', ''), '-', '') LIKE ? ORDER BY nome";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + busca + "%");
            String cleanBusca = busca.replaceAll("[^0-9]", "");
            stmt.setString(2, "%" + cleanBusca + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Morador m = new Morador();
                    m.setId(rs.getInt("id"));
                    m.setNome(rs.getString("nome"));
                    m.setCpf(rs.getString("cpf"));
                    m.setLogin(rs.getString("login"));
                    m.setSenha(rs.getString("senha"));
                    m.setApartamentoId(rs.getInt("apartamento_id"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao buscar morador por nome ou CPF.", e);
        }
        return lista;
    }

    public boolean existeCpf(String cpf) throws DAOException {
        String sql = "SELECT 1 FROM moradores WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String cleanCpf = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";
            stmt.setString(1, cleanCpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Se houver próximo, existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao verificar CPF do morador.", e);
        }
    }

    public Morador buscarPorCpf(String cpf) throws DAOException {
        String sql = "SELECT * FROM moradores WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String cleanCpf = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";
            stmt.setString(1, cleanCpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Morador m = new Morador();
                    m.setId(rs.getInt("id"));
                    m.setNome(rs.getString("nome"));
                    m.setCpf(rs.getString("cpf"));
                    m.setLogin(rs.getString("login"));
                    m.setSenha(rs.getString("senha"));
                    m.setApartamentoId(rs.getInt("apartamento_id"));
                    return m;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao buscar morador por CPF.", e);
        }
        return null;
    }

    public Morador autenticar(String login, String senha) throws DAOException {
        String sql = "SELECT * FROM moradores WHERE login = ? AND senha = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, senha);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Morador m = new Morador();
                    m.setId(rs.getInt("id"));
                    m.setNome(rs.getString("nome"));
                    m.setCpf(rs.getString("cpf"));
                    m.setLogin(rs.getString("login"));
                    m.setSenha(rs.getString("senha"));
                    m.setApartamentoId(rs.getInt("apartamento_id"));
                    return m;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro na autenticação do morador.", e);
        }
        return null;
    }

    public void atualizar(Morador m) throws DAOException {
        String sql = "UPDATE moradores SET nome = ?, cpf = ?, login = ?, senha = ?, apartamento_id = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNome());
            
            String cleanCpf = m.getCpf() != null ? m.getCpf().replaceAll("[^0-9]", "") : "";
            stmt.setString(2, cleanCpf);
            
            stmt.setString(3, m.getLogin());
            stmt.setString(4, m.getSenha());
            stmt.setInt(5, m.getApartamentoId());
            stmt.setInt(6, m.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao atualizar morador.", e);
        }
    }

}
