package br.com.reservadaaldeia.portaria.dao;

import br.com.reservadaaldeia.portaria.database.DatabaseHelper;
import br.com.reservadaaldeia.portaria.model.Visitante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.reservadaaldeia.portaria.exception.DAOException;

public class VisitanteDAO {

    public void registrarEntrada(Visitante v) throws DAOException {
        String sql = "INSERT INTO visitantes(nome, cpf, apartamento_id, morador_id, data_entrada) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getNome());
            String cleanCpf = v.getCpf() != null ? v.getCpf().replaceAll("[^0-9]", "") : "";
            stmt.setString(2, cleanCpf);
            stmt.setInt(3, v.getApartamentoId());
            stmt.setInt(4, v.getMoradorId());
            stmt.setString(5, v.getDataEntrada());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao registrar entrada de visitante.", e);
        }
    }

    public void registrarSaida(int visitanteId, String dataSaida) throws DAOException {
        String sql = "UPDATE visitantes SET data_saida = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dataSaida);
            stmt.setInt(2, visitanteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao registrar saída de visitante.", e);
        }
    }

    public List<Visitante> listarAtivos() throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM visitantes WHERE data_saida IS NULL ORDER BY data_entrada DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Visitante v = new Visitante();
                v.setId(rs.getInt("id"));
                v.setNome(rs.getString("nome"));
                v.setCpf(rs.getString("cpf"));
                v.setApartamentoId(rs.getInt("apartamento_id"));
                v.setMoradorId(rs.getInt("morador_id"));
                v.setDataEntrada(rs.getString("data_entrada"));
                v.setDataSaida(rs.getString("data_saida"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar visitantes ativos.", e);
        }
        return lista;
    }

    public List<Visitante> listarPorMorador(int moradorId) throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM visitantes WHERE morador_id = ? ORDER BY data_entrada DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moradorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Visitante v = new Visitante();
                    v.setId(rs.getInt("id"));
                    v.setNome(rs.getString("nome"));
                    v.setCpf(rs.getString("cpf"));
                    v.setApartamentoId(rs.getInt("apartamento_id"));
                    v.setMoradorId(rs.getInt("morador_id"));
                    v.setDataEntrada(rs.getString("data_entrada"));
                    v.setDataSaida(rs.getString("data_saida"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar visitantes por morador.", e);
        }
        return lista;
    }

    public List<Visitante> listarTodos() throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM visitantes ORDER BY data_entrada DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Visitante v = new Visitante();
                v.setId(rs.getInt("id"));
                v.setNome(rs.getString("nome"));
                v.setCpf(rs.getString("cpf"));
                v.setApartamentoId(rs.getInt("apartamento_id"));
                v.setMoradorId(rs.getInt("morador_id"));
                v.setDataEntrada(rs.getString("data_entrada"));
                v.setDataSaida(rs.getString("data_saida"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar todos os visitantes.", e);
        }
        return lista;
    }

    public List<Visitante> listarInativos() throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT * FROM visitantes WHERE data_saida IS NOT NULL ORDER BY data_saida DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Visitante v = new Visitante();
                v.setId(rs.getInt("id"));
                v.setNome(rs.getString("nome"));
                v.setCpf(rs.getString("cpf"));
                v.setApartamentoId(rs.getInt("apartamento_id"));
                v.setMoradorId(rs.getInt("morador_id"));
                v.setDataEntrada(rs.getString("data_entrada"));
                v.setDataSaida(rs.getString("data_saida"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar visitantes inativos.", e);
        }
        return lista;
    }

    public void deletar(int visitanteId) throws DAOException {
        String sql = "DELETE FROM visitantes WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitanteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao deletar visitante.", e);
        }
    }
}
