package portaria.dao;

import portaria.database.DatabaseHelper;
import portaria.model.Visitante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import portaria.exception.DAOException;
import portaria.util.ValidadorEntrada;

public class VisitanteDAO {

    public void registrarEntrada(Visitante v) throws DAOException {
        ValidadorEntrada.validarNome(v.getNome(), "Nome do Visitante");
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // Iniciar transação
            try {
                String cleanCpf = v.getCpf() != null ? v.getCpf().replaceAll("[^0-9]", "") : "";
                int visitanteId = -1;

                // 1. Verifica se o visitante já existe
                String sqlBusca = "SELECT id FROM visitantes WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
                try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBusca)) {
                    stmtBusca.setString(1, cleanCpf);
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (rs.next()) {
                            visitanteId = rs.getInt("id");
                        }
                    }
                }

                // 2. Se não existe, insere o visitante
                if (visitanteId == -1) {
                    String sqlInsertVisitante = "INSERT INTO visitantes(nome, cpf) VALUES(?,?)";
                    try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertVisitante, Statement.RETURN_GENERATED_KEYS)) {
                        stmtInsert.setString(1, v.getNome());
                        stmtInsert.setString(2, cleanCpf);
                        stmtInsert.executeUpdate();
                        try (ResultSet rs = stmtInsert.getGeneratedKeys()) {
                            if (rs.next()) {
                                visitanteId = rs.getInt(1);
                            }
                        }
                    }
                } else {
                    // Atualiza o nome do visitante se já existir
                    String sqlUpdateNome = "UPDATE visitantes SET nome = ? WHERE id = ?";
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateNome)) {
                        stmtUpdate.setString(1, v.getNome());
                        stmtUpdate.setInt(2, visitanteId);
                        stmtUpdate.executeUpdate();
                    }
                }

                // 3. Insere a visita (O v.getId() em outras partes do app passa a ser o id da Visita)
                String sqlVisita = "INSERT INTO visitas(visitante_id, apartamento_id, morador_id, data_entrada) VALUES(?,?,?,?)";
                try (PreparedStatement stmtVisita = conn.prepareStatement(sqlVisita)) {
                    stmtVisita.setInt(1, visitanteId);
                    stmtVisita.setInt(2, v.getApartamentoId());
                    stmtVisita.setInt(3, v.getMoradorId());
                    stmtVisita.setString(4, v.getDataEntrada());
                    stmtVisita.executeUpdate();
                }

                conn.commit(); // Confirmar transação
            } catch (SQLException ex) {
                conn.rollback(); // Reverter se der erro
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao registrar entrada de visitante.", e);
        }
    }

    public void registrarSaida(int visitaId, String dataSaida) throws DAOException {
        String sql = "UPDATE visitas SET data_saida = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dataSaida);
            stmt.setInt(2, visitaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao registrar saída de visitante.", e);
        }
    }

    public List<Visitante> listarAtivos() throws DAOException {
        return listarGenerico("SELECT v.id, p.nome, p.cpf, v.apartamento_id, v.morador_id, v.data_entrada, v.data_saida " +
                              "FROM visitas v JOIN visitantes p ON v.visitante_id = p.id " +
                              "WHERE v.data_saida IS NULL ORDER BY v.data_entrada DESC");
    }

    public List<Visitante> listarPorMorador(int moradorId) throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT v.id, p.nome, p.cpf, v.apartamento_id, v.morador_id, v.data_entrada, v.data_saida " +
                     "FROM visitas v JOIN visitantes p ON v.visitante_id = p.id " +
                     "WHERE v.morador_id = ? ORDER BY v.data_entrada DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moradorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao listar visitantes por morador.", e);
        }
        return lista;
    }

    public List<Visitante> listarTodos() throws DAOException {
        return listarGenerico("SELECT v.id, p.nome, p.cpf, v.apartamento_id, v.morador_id, v.data_entrada, v.data_saida " +
                              "FROM visitas v JOIN visitantes p ON v.visitante_id = p.id " +
                              "ORDER BY v.data_entrada DESC");
    }

    public List<Visitante> listarInativos() throws DAOException {
        return listarGenerico("SELECT v.id, p.nome, p.cpf, v.apartamento_id, v.morador_id, v.data_entrada, v.data_saida " +
                              "FROM visitas v JOIN visitantes p ON v.visitante_id = p.id " +
                              "WHERE v.data_saida IS NOT NULL ORDER BY v.data_saida DESC");
    }

    public void deletar(int visitaId) throws DAOException {
        String sql = "DELETE FROM visitas WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao deletar visita.", e);
        }
    }

    private List<Visitante> listarGenerico(String sql) throws DAOException {
        List<Visitante> lista = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Erro ao buscar registros de visitantes.", e);
        }
        return lista;
    }

    private Visitante mapearResultSet(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNome(rs.getString("nome"));
        v.setCpf(rs.getString("cpf"));
        v.setApartamentoId(rs.getInt("apartamento_id"));
        v.setMoradorId(rs.getInt("morador_id"));
        v.setDataEntrada(rs.getString("data_entrada"));
        v.setDataSaida(rs.getString("data_saida"));
        return v;
    }
}

