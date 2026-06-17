package portaria.dao;

import portaria.database.DatabaseHelper;
import portaria.model.Encomenda;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import portaria.exception.DAOException;
import portaria.util.ValidadorEntrada;

public class EncomendaDAO {

    public void registrarRecebimento(Encomenda e) throws DAOException {
        ValidadorEntrada.validarNome(e.getEntregador(), "Nome do Entregador");
        ValidadorEntrada.validarNome(e.getRecebedor(), "Nome do Recebedor");
        String sql = "INSERT INTO encomendas(id, morador_id, entregador, recebedor, data_recebimento, status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getId());
            stmt.setInt(2, e.getMoradorId());
            stmt.setString(3, e.getEntregador());
            stmt.setString(4, e.getRecebedor());
            stmt.setString(5, e.getDataRecebimento());
            stmt.setString(6, e.getStatus());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DAOException("Erro ao registrar encomenda.", ex);
        }
    }

    public void atualizarStatus(String encomendaId, String novoStatus) throws DAOException {
        String sql = "UPDATE encomendas SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setString(2, encomendaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao atualizar status da encomenda.", e);
        }
    }

    public List<Encomenda> listarTodas() throws DAOException {
        List<Encomenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM encomendas ORDER BY data_recebimento DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Encomenda e = new Encomenda();
                e.setId(rs.getString("id"));
                e.setMoradorId(rs.getInt("morador_id"));
                e.setEntregador(rs.getString("entregador"));
                e.setRecebedor(rs.getString("recebedor"));
                e.setDataRecebimento(rs.getString("data_recebimento"));
                e.setStatus(rs.getString("status"));
                lista.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar encomendas.", e);
        }
        return lista;
    }

    public List<Encomenda> listarPorMorador(int moradorId) throws DAOException {
        List<Encomenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM encomendas WHERE morador_id = ? ORDER BY data_recebimento DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, moradorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Encomenda e = new Encomenda();
                    e.setId(rs.getString("id"));
                    e.setMoradorId(rs.getInt("morador_id"));
                    e.setEntregador(rs.getString("entregador"));
                    e.setRecebedor(rs.getString("recebedor"));
                    e.setDataRecebimento(rs.getString("data_recebimento"));
                    e.setStatus(rs.getString("status"));
                    lista.add(e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar encomendas por morador.", e);
        }
        return lista;
    }
}

