package portaria.view;

import portaria.model.Morador;
import portaria.model.Encomenda;
import portaria.dao.EncomendaDAO;
import portaria.exception.DAOException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MoradorMainView extends JFrame {

    private Morador moradorLogado;
    private EncomendaDAO encomendaDAO;
    private DefaultTableModel tableModel;

    public MoradorMainView(Morador moradorLogado) {
        this.moradorLogado = moradorLogado;
        this.encomendaDAO = new EncomendaDAO();

        setTitle("PortariaSegura - Morador: " + moradorLogado.getNome());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Minhas Encomendas", criarPainelEncomendas());
        tabbedPane.addTab("Meus Visitantes", criarPainelVisitantes());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JButton btnLogout = new JButton("Sair / Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginView().setVisible(true);
        });
        headerPanel.add(new JLabel("Bem-vindo(a), " + moradorLogado.getNome()), BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel criarPainelEncomendas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Minhas Encomendas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, BorderLayout.NORTH);

        String[] colunas = {"Código", "Data/Hora", "Entregador", "Recebedor", "Status"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar Lista");
        JCheckBox chkApenasPendentes = new JCheckBox("Apenas Pendentes", true);
        
        JPanel panelBotoes = new JPanel();
        panelBotoes.add(chkApenasPendentes);
        panelBotoes.add(btnAtualizar);
        panel.add(panelBotoes, BorderLayout.SOUTH);

        Runnable carregarDados = () -> {
            tableModel.setRowCount(0);
            try {
                List<Encomenda> encomendas = encomendaDAO.listarPorMorador(moradorLogado.getId());
                boolean soPendentes = chkApenasPendentes.isSelected();
                for (Encomenda e : encomendas) {
                    if (soPendentes && !e.getStatus().equalsIgnoreCase("PENDENTE")) {
                        continue;
                    }
                    tableModel.addRow(new Object[]{
                            e.getId(), e.getDataRecebimento(), e.getEntregador(), e.getRecebedor(), e.getStatus()
                    });
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar encomendas:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };

        carregarDados.run();

        btnAtualizar.addActionListener(e -> carregarDados.run());
        chkApenasPendentes.addActionListener(e -> carregarDados.run());

        return panel;
    }

    private JPanel criarPainelVisitantes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Histórico de Visitas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, BorderLayout.NORTH);

        String[] colunas = {"Nome", "CPF", "Data de Entrada", "Data de Saída"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar Lista");
        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnAtualizar);
        panel.add(panelBotoes, BorderLayout.SOUTH);

        portaria.dao.VisitanteDAO visDAO = new portaria.dao.VisitanteDAO();
        
        Runnable carregarDados = () -> {
            model.setRowCount(0);
            try {
                List<portaria.model.Visitante> visitantes = visDAO.listarPorMorador(moradorLogado.getId());
                for (portaria.model.Visitante v : visitantes) {
                    String cpfExibicao = portaria.util.ValidadorCPF.formatarEObscurecerCPF(v.getCpf(), "MORADOR");
                    String dataSaida = v.getDataSaida() != null ? v.getDataSaida() : "Ainda no condomínio";
                    model.addRow(new Object[]{
                            v.getNome(), cpfExibicao, v.getDataEntrada(), dataSaida
                    });
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar visitantes:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };

        carregarDados.run();
        btnAtualizar.addActionListener(e -> carregarDados.run());

        return panel;
    }
}

