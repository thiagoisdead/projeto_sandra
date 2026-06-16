package br.com.reservadaaldeia.portaria.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    
    private static final String URL = "jdbc:sqlite:portaria.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabela de Usuários (Síndico e Porteiro)
            String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id TEXT PRIMARY KEY," +
                    "login TEXT UNIQUE NOT NULL," +
                    "senha TEXT NOT NULL," +
                    "tipo TEXT NOT NULL," + // SINDICO ou PORTEIRO
                    "cpf TEXT" + // CPF anonymization
                    ");";
            stmt.execute(sqlUsuarios);

            // Tabela de Apartamentos
            String sqlApartamentos = "CREATE TABLE IF NOT EXISTS apartamentos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "torre INTEGER NOT NULL," +
                    "bloco INTEGER NOT NULL," +
                    "andar INTEGER NOT NULL," +
                    "numero INTEGER NOT NULL" +
                    ");";
            stmt.execute(sqlApartamentos);

            // Tabela de Moradores
            String sqlMoradores = "CREATE TABLE IF NOT EXISTS moradores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "cpf TEXT UNIQUE," +
                    "login TEXT," +
                    "senha TEXT," +
                    "apartamento_id INTEGER," +
                    "FOREIGN KEY(apartamento_id) REFERENCES apartamentos(id)" +
                    ");";
            stmt.execute(sqlMoradores);

            // Tabela de Visitantes
            String sqlVisitantes = "CREATE TABLE IF NOT EXISTS visitantes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "cpf TEXT," +
                    "apartamento_id INTEGER," +
                    "morador_id INTEGER NOT NULL," +
                    "data_entrada TEXT NOT NULL," +
                    "data_saida TEXT," +
                    "FOREIGN KEY(apartamento_id) REFERENCES apartamentos(id)," +
                    "FOREIGN KEY(morador_id) REFERENCES moradores(id)" +
                    ");";
            stmt.execute(sqlVisitantes);

            // Tabela de Encomendas
            String sqlEncomendas = "CREATE TABLE IF NOT EXISTS encomendas (" +
                    "id TEXT PRIMARY KEY," +
                    "morador_id INTEGER NOT NULL," +
                    "entregador TEXT NOT NULL," +
                    "recebedor TEXT NOT NULL," +
                    "data_recebimento TEXT NOT NULL," +
                    "status TEXT NOT NULL," + // PENDENTE ou ENTREGUE
                    "FOREIGN KEY(morador_id) REFERENCES moradores(id)" +
                    ");";
            stmt.execute(sqlEncomendas);

            // Inserir um usuário admin padrão se não existir (apenas o Síndico inicial)
            // Usando CPF válido: 11144477735
            String adminId = java.util.UUID.randomUUID().toString().replace("-", "");
            String sqlAdmin = "INSERT OR IGNORE INTO usuarios (id, login, senha, tipo, cpf) VALUES ('" + adminId + "', 'admin', 'admin', 'SINDICO', '11144477735')";
            stmt.execute(sqlAdmin);
            
            System.out.println("Banco de dados inicializado com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String verificarPapelCpf(String cpf) {
        String cleanCpf = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";
        if (cleanCpf.isEmpty()) return null;

        try (Connection conn = getConnection()) {
            // Check usuarios
            String sqlUsuarios = "SELECT 1 FROM usuarios WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlUsuarios)) {
                stmt.setString(1, cleanCpf);
                if (stmt.executeQuery().next()) return "USUARIO";
            }
            // Check moradores
            String sqlMoradores = "SELECT 1 FROM moradores WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlMoradores)) {
                stmt.setString(1, cleanCpf);
                if (stmt.executeQuery().next()) return "MORADOR";
            }
            // Check visitantes
            String sqlVisitantes = "SELECT 1 FROM visitantes WHERE replace(replace(cpf, '.', ''), '-', '') = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlVisitantes)) {
                stmt.setString(1, cleanCpf);
                if (stmt.executeQuery().next()) return "VISITANTE";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
