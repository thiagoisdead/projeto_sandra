package br.com.reservadaaldeia.portaria.dao;

import br.com.reservadaaldeia.portaria.database.DatabaseHelper;
import br.com.reservadaaldeia.portaria.model.Apartamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.reservadaaldeia.portaria.exception.DAOException;

public class ApartamentoDAO {
    
    public int inserir(Apartamento apto) throws DAOException {
        String sql = "INSERT INTO apartamentos(torre, bloco, andar, numero) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, apto.getTorre());
            stmt.setInt(2, apto.getBloco());
            stmt.setInt(3, apto.getAndar());
            stmt.setInt(4, apto.getNumero());
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao cadastrar apartamento no banco de dados.", e);
        }
        return -1;
    }

    public List<Apartamento> listarTodos() throws DAOException {
        List<Apartamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM apartamentos ORDER BY torre, bloco, andar, numero";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Apartamento apto = new Apartamento();
                apto.setId(rs.getInt("id"));
                apto.setTorre(rs.getInt("torre"));
                apto.setBloco(rs.getInt("bloco"));
                apto.setAndar(rs.getInt("andar"));
                apto.setNumero(rs.getInt("numero"));
                lista.add(apto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar apartamentos.", e);
        }
        return lista;
    }

    public Apartamento buscarPorId(int id) throws DAOException {
        String sql = "SELECT * FROM apartamentos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Apartamento apto = new Apartamento();
                    apto.setId(rs.getInt("id"));
                    apto.setTorre(rs.getInt("torre"));
                    apto.setBloco(rs.getInt("bloco"));
                    apto.setAndar(rs.getInt("andar"));
                    apto.setNumero(rs.getInt("numero"));
                    return apto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao buscar apartamento.", e);
        }
        return null;
    }

    public void atualizar(Apartamento apto) throws DAOException {
        String sql = "UPDATE apartamentos SET torre = ?, bloco = ?, andar = ?, numero = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, apto.getTorre());
            stmt.setInt(2, apto.getBloco());
            stmt.setInt(3, apto.getAndar());
            stmt.setInt(4, apto.getNumero());
            stmt.setInt(5, apto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao atualizar apartamento.", e);
        }
    }

}
