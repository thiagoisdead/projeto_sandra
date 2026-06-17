package portaria.view;

import portaria.model.Usuario;
import portaria.dao.UsuarioDAO;
import portaria.model.Morador;
import portaria.dao.MoradorDAO;
import portaria.model.Apartamento;
import portaria.dao.ApartamentoDAO;
import portaria.model.Encomenda;
import portaria.dao.EncomendaDAO;
import portaria.model.Visitante;
import portaria.dao.VisitanteDAO;
import portaria.util.ValidadorCPF;
import portaria.exception.DAOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MainView extends JFrame {

    private Usuario usuarioLogado;
    private JTabbedPane tabbedPane;

    public MainView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        
        setTitle("PortariaSegura - Condomínio Reserva da Aldeia | Usuário: " + usuarioLogado.getLogin());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // Adiciona as abas
        tabbedPane.addTab("Visitantes", criarPainelVisitantes());
        tabbedPane.addTab("Encomendas", criarPainelEncomendas());
        tabbedPane.addTab("Moradores", criarPainelMoradores());
        
        // Aba exclusiva para síndico (administração básica)
        if ("SINDICO".equals(usuarioLogado.getTipo())) {
            tabbedPane.addTab("Administração", criarPainelAdministracao());
        }
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JButton btnLogout = new JButton("Sair / Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginView().setVisible(true);
        });
        headerPanel.add(new JLabel("Bem-vindo(a), " + usuarioLogado.getLogin() + " (" + usuarioLogado.getTipo() + ")"), BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel criarPainelVisitantes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Controle e Histórico de Visitantes");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(lblTitulo, BorderLayout.WEST);
        
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filtrar:"));
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"No Condomínio", "Já Saíram", "Todos os Registros"});
        filterPanel.add(cbFiltro);
        topPanel.add(filterPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] colunas = {"ID", "Nome", "CPF", "Apto de Destino", "Data/Hora Entrada", "Data/Hora Saída", "Ações"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        
        table.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText("🗑️ Excluir");
                label.setHorizontalAlignment(javax.swing.JLabel.CENTER);
                label.setForeground(java.awt.Color.RED);
                label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                return label;
            }
        });
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel panelBotoes = new JPanel();
        JButton btnEntrada = new JButton("Registrar Entrada");
        JButton btnSaida = new JButton("Registrar Saída");
        panelBotoes.add(btnEntrada);
        panelBotoes.add(btnSaida);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        VisitanteDAO visitanteDAO = new VisitanteDAO();
        ApartamentoDAO aptoDAO = new ApartamentoDAO();

        Runnable recarregarTabela = () -> {
            tableModel.setRowCount(0);
            try {
                java.util.Map<Integer, String> aptoExibicaoMap = new java.util.HashMap<>();
                for (Apartamento a : aptoDAO.listarTodos()) {
                    aptoExibicaoMap.put(a.getId(), a.toString());
                }

                java.util.List<Visitante> lista;
                String filtro = (String) cbFiltro.getSelectedItem();
                if ("Já Saíram".equals(filtro)) {
                    lista = visitanteDAO.listarInativos();
                } else if ("Todos os Registros".equals(filtro)) {
                    lista = visitanteDAO.listarTodos();
                } else {
                    lista = visitanteDAO.listarAtivos(); // "No Condomínio"
                }
                
                for (Visitante v : lista) {
                    String cpfExibicao = ValidadorCPF.formatarEObscurecerCPF(v.getCpf(), usuarioLogado.getTipo());
                    String aptoExibicao = aptoExibicaoMap.getOrDefault(v.getApartamentoId(), "ID: " + v.getApartamentoId());
                    String saida = (v.getDataSaida() != null) ? v.getDataSaida() : "-";
                    tableModel.addRow(new Object[]{
                        v.getId(), v.getNome(), cpfExibicao, aptoExibicao, v.getDataEntrada(), saida, "Excluir"
                    });
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar visitantes:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
        recarregarTabela.run();
        
        cbFiltro.addActionListener(e -> recarregarTabela.run());
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = table.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / table.getRowHeight();
                
                if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                    if (column == 6) { // Coluna Ações
                        if (!"SINDICO".equals(usuarioLogado.getTipo())) {
                            JOptionPane.showMessageDialog(MainView.this, "Apenas o Síndico pode excluir registros do histórico.", "Acesso Negado", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        int visitanteId = (int) tableModel.getValueAt(row, 0);
                        String nome = (String) tableModel.getValueAt(row, 1);
                        
                        int confirm = JOptionPane.showConfirmDialog(MainView.this, 
                            "Deseja excluir permanentemente o registro de visitante '" + nome + "' do histórico?",
                            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                visitanteDAO.deletar(visitanteId);
                                recarregarTabela.run();
                                JOptionPane.showMessageDialog(MainView.this, "Registro deletado com sucesso!");
                            } catch (DAOException ex) {
                                JOptionPane.showMessageDialog(MainView.this, "Erro ao excluir visitante:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        btnEntrada.addActionListener(e -> {
            String nomeVisitante = JOptionPane.showInputDialog(this, "Nome do Visitante:");
            if (nomeVisitante == null || nomeVisitante.trim().isEmpty()) return;

            String cpfVisitante = JOptionPane.showInputDialog(this, "CPF do Visitante:");
            if (cpfVisitante == null || cpfVisitante.trim().isEmpty()) return;
            if (!ValidadorCPF.isCPF(cpfVisitante)) {
                JOptionPane.showMessageDialog(this, "CPF do Visitante inválido.");
                return;
            }

            String papelVisitante = portaria.database.DatabaseHelper.verificarPapelCpf(cpfVisitante);
            if (papelVisitante != null && !papelVisitante.equals("VISITANTE")) {
                JOptionPane.showMessageDialog(this, "Este CPF pertence a um " + papelVisitante + " e não pode ser usado para visitante.");
                return;
            }

            String cpfMorador = JOptionPane.showInputDialog(this, "CPF do Morador de Destino:");
            if (cpfMorador == null || cpfMorador.trim().isEmpty()) return;

            try {
                MoradorDAO moradorDAO = new MoradorDAO();
                Morador morador = moradorDAO.buscarPorCpf(cpfMorador);
                if (morador == null) {
                    JOptionPane.showMessageDialog(this, "Morador não encontrado com o CPF informado.");
                    return;
                }

                Apartamento apto = aptoDAO.buscarPorId(morador.getApartamentoId());
                String aptoStr = (apto != null) ? apto.toString() : "ID: " + morador.getApartamentoId();

                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Confirmar entrada do visitante?\n\n" +
                    "Nome: " + nomeVisitante + "\n" +
                    "CPF: " + ValidadorCPF.formatarEObscurecerCPF(cpfVisitante, usuarioLogado.getTipo()) + "\n" +
                    "Destino: Apartamento " + aptoStr + " (Morador: " + morador.getNome() + ")",
                    "Confirmar Entrada", JOptionPane.YES_NO_OPTION);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    String dataEntrada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    Visitante v = new Visitante(0, nomeVisitante, cpfVisitante, morador.getApartamentoId(), morador.getId(), dataEntrada, null);
                    visitanteDAO.registrarEntrada(v);
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Entrada do visitante registrada com sucesso!");
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSaida.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um visitante na tabela para registrar a saída.");
                return;
            }
            
            String saidaAtual = (String) tableModel.getValueAt(row, 5);
            if (!"-".equals(saidaAtual)) {
                JOptionPane.showMessageDialog(this, "A saída deste visitante já foi registrada anteriormente.");
                return;
            }
            
            int visitanteId = (int) tableModel.getValueAt(row, 0);
            String nome = (String) tableModel.getValueAt(row, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Confirmar a saída do visitante '" + nome + "'?",
                "Confirmar Saída", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String dataSaida = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    visitanteDAO.registrarSaida(visitanteId, dataSaida);
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Saída registrada!");
                } catch (DAOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel criarPainelEncomendas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Controle de Encomendas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusca.add(new JLabel("CPF do Morador:"));
        JTextField txtCpf = new JTextField(12);
        panelBusca.add(txtCpf);
        JButton btnBuscar = new JButton("Pesquisar Encomendas");
        JButton btnNova = new JButton("Nova Encomenda");
        panelBusca.add(btnBuscar);
        panelBusca.add(btnNova);
        topPanel.add(panelBusca, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] colunas = {"Código", "Dono (ID Morador)", "Entregador", "Recebedor", "Data/Hora", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel panelBotoes = new JPanel();
        JButton btnMudarStatus = new JButton("Alternar Status / Marcar Entregue");
        panelBotoes.add(btnMudarStatus);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        EncomendaDAO encomendaDAO = new EncomendaDAO();
        MoradorDAO moradorDAO = new MoradorDAO();

        Runnable recarregarTabela = () -> {
            tableModel.setRowCount(0);
            String cpfFiltro = txtCpf.getText().trim();
            java.util.List<Encomenda> lista;
            
            try {
                if (cpfFiltro.isEmpty()) {
                    lista = encomendaDAO.listarTodas();
                } else {
                    Morador m = moradorDAO.buscarPorCpf(cpfFiltro);
                    if (m != null) {
                        lista = encomendaDAO.listarPorMorador(m.getId());
                    } else {
                        lista = new java.util.ArrayList<>();
                    }
                }
                
                for (Encomenda e : lista) {
                    tableModel.addRow(new Object[]{
                        e.getId(), e.getMoradorId(), e.getEntregador(), e.getRecebedor(), e.getDataRecebimento(), e.getStatus()
                    });
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar encomendas:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
        recarregarTabela.run();

        btnBuscar.addActionListener(e -> {
            String cpf = txtCpf.getText().trim();
            if (!cpf.isEmpty() && !ValidadorCPF.isCPF(cpf)) {
                JOptionPane.showMessageDialog(this, "CPF inválido.");
                return;
            }
            recarregarTabela.run();
        });

        btnNova.addActionListener(e -> {
            String cpf = txtCpf.getText().trim();
            if (cpf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o CPF do morador para registrar a encomenda.");
                return;
            }
            if (!ValidadorCPF.isCPF(cpf)) {
                JOptionPane.showMessageDialog(this, "CPF inválido.");
                return;
            }
            try {
                Morador morador = moradorDAO.buscarPorCpf(cpf);
                if (morador == null) {
                    JOptionPane.showMessageDialog(this, "Morador não encontrado para este CPF.");
                    return;
                }

                String entregador = JOptionPane.showInputDialog(this, "Nova encomenda para: " + morador.getNome() + "\nQuem está entregando? (ex: Correios)");
                if (entregador != null && !entregador.trim().isEmpty()) {
                    String idAleatorio = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    String recebedor = usuarioLogado.getLogin();

                    Encomenda enc = new Encomenda(idAleatorio, morador.getId(), entregador, recebedor, dataAtual, "PENDENTE");
                    encomendaDAO.registrarRecebimento(enc);
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Encomenda registrada! Código: " + idAleatorio);
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar encomenda:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnMudarStatus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma encomenda na tabela.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            String statusAtual = (String) tableModel.getValueAt(row, 5);

            try {
                if ("PORTEIRO".equals(usuarioLogado.getTipo())) {
                    if ("ENTREGUE".equals(statusAtual)) {
                        JOptionPane.showMessageDialog(this, "O Porteiro não pode mudar uma encomenda Entregue de volta para Pendente.");
                        return;
                    }
                    encomendaDAO.atualizarStatus(id, "ENTREGUE");
                } else if ("SINDICO".equals(usuarioLogado.getTipo())) {
                    String novoStatus = "PENDENTE".equals(statusAtual) ? "ENTREGUE" : "PENDENTE";
                    encomendaDAO.atualizarStatus(id, novoStatus);
                }

                recarregarTabela.run();
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar status:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }

    private JPanel criarPainelMoradores() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusca.add(new JLabel("Buscar por Nome ou CPF:"));
        JTextField txtBusca = new JTextField(20);
        panelBusca.add(txtBusca);
        JButton btnBuscar = new JButton("Buscar");
        panelBusca.add(btnBuscar);
        JButton btnRefresh = new JButton("Atualizar / Limpar");
        panelBusca.add(btnRefresh);
        panel.add(panelBusca, BorderLayout.NORTH);
        
        String[] colunas = {"ID", "Nome", "CPF", "Apartamento"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        MoradorDAO moradorDAO = new MoradorDAO();
        ApartamentoDAO aptoDAO = new ApartamentoDAO();
        
        Runnable recarregarTabela = () -> {
            tableModel.setRowCount(0);
            
            try {
                java.util.Map<Integer, String> aptoExibicaoMap = new java.util.HashMap<>();
                for (Apartamento a : aptoDAO.listarTodos()) {
                    aptoExibicaoMap.put(a.getId(), a.toString());
                }

                String busca = txtBusca.getText().trim();
                java.util.List<Morador> lista;
                if (busca.isEmpty()) {
                    lista = moradorDAO.listarTodos();
                } else {
                    lista = moradorDAO.buscarPorNomeOuCpf(busca);
                }
                
                for (Morador m : lista) {
                    String cpfExibicao = ValidadorCPF.formatarEObscurecerCPF(m.getCpf(), usuarioLogado.getTipo());
                    String aptoExibicao = aptoExibicaoMap.getOrDefault(m.getApartamentoId(), "ID: " + m.getApartamentoId());
                    tableModel.addRow(new Object[]{
                        m.getId(), m.getNome(), cpfExibicao, aptoExibicao
                    });
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar moradores:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
        
        recarregarTabela.run();
        
        btnBuscar.addActionListener(e -> recarregarTabela.run());
        btnRefresh.addActionListener(e -> {
            txtBusca.setText("");
            recarregarTabela.run();
        });
        
        return panel;
    }

    private JPanel criarPainelAdministracao() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Usuários / Porteiros", criarPainelGerenciarUsuarios());
        adminTabs.addTab("Moradores e Apartamentos", criarPainelGerenciarMoradores());
        mainPanel.add(adminTabs, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel criarPainelGerenciarUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Gerenciamento de Porteiros e Usuários do Condomínio");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Painel de formulário (Esquerda)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Usuário"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        JTextField txtLogin = new JTextField(15);
        formPanel.add(txtLogin, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtSenha = new JPasswordField(15);
        formPanel.add(txtSenha, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"PORTEIRO", "SINDICO"});
        formPanel.add(cbTipo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1;
        JTextField txtCpf = new JTextField(15);
        formPanel.add(txtCpf, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JButton btnSalvar = new JButton("Cadastrar");
        formPanel.add(btnSalvar, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        JButton btnRefresh = new JButton("Atualizar Tabela");
        formPanel.add(btnRefresh, gbc);
        
        panel.add(formPanel, BorderLayout.WEST);
        
        // Painel de listagem e exclusão (Centro)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Usuários Cadastrados"));
        
        String[] colunas = {"ID (Hash)", "Login", "Tipo", "CPF"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnExcluir = new JButton("Excluir Selecionado");
        JButton btnEditar = new JButton("Editar Selecionado");
        JPanel botoesTable = new JPanel();
        botoesTable.add(btnExcluir);
        botoesTable.add(btnEditar);
        tablePanel.add(botoesTable, BorderLayout.SOUTH);
        
        panel.add(tablePanel, BorderLayout.CENTER);
        
        // Instancia DAO de Usuários
        UsuarioDAO dao = new UsuarioDAO();
        
        // Método local para recarregar a tabela
        Runnable recarregarTabela = () -> {
            tableModel.setRowCount(0);
            try {
                java.util.List<Usuario> usuarios = dao.listarTodos();
                for (Usuario u : usuarios) {
                    String cpfExibicao = ValidadorCPF.formatarEObscurecerCPF(u.getCpf(), usuarioLogado.getTipo());
                    tableModel.addRow(new Object[]{u.getId(), u.getLogin(), u.getTipo(), cpfExibicao});
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar usuários:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
        recarregarTabela.run();
        
        btnRefresh.addActionListener(e -> recarregarTabela.run());
        
        // Evento de Salvar
        btnSalvar.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String senha = new String(txtSenha.getPassword()).trim();
            String tipo = (String) cbTipo.getSelectedItem();
            String cpf = txtCpf.getText().trim();
            
            if (login.isEmpty() || senha.isEmpty() || cpf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return;
            }
            
            if (!ValidadorCPF.isCPF(cpf)) {
                JOptionPane.showMessageDialog(this, "O CPF informado é inválido.");
                return;
            }
            
            String papel = portaria.database.DatabaseHelper.verificarPapelCpf(cpf);
            if (papel != null) {
                JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado no sistema como " + papel + ".");
                return;
            }

            try {
                if (dao.existeLogin(login)) {
                    JOptionPane.showMessageDialog(this, "Este Login já está em uso. Escolha outro.");
                    return;
                }
                
                Usuario novo = new Usuario();
                novo.setLogin(login);
                novo.setSenha(senha);
                novo.setTipo(tipo);
                novo.setCpf(cpf);
                
                if (dao.inserir(novo)) {
                    txtLogin.setText("");
                    txtSenha.setText("");
                    txtCpf.setText("");
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário. Verifique os dados.");
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Evento de Excluir
        btnExcluir.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.");
                return;
            }
            
            String id = (String) tableModel.getValueAt(row, 0);
            String login = (String) tableModel.getValueAt(row, 1);
            
            if ("admin".equals(login)) {
                JOptionPane.showMessageDialog(this, "Não é possível excluir o usuário administrador padrão.");
                return;
            }
            
            if (usuarioLogado.getId().equals(id)) {
                JOptionPane.showMessageDialog(this, "Você não pode excluir a si mesmo.");
                return;
            }
            
            int confirmar = JOptionPane.showConfirmDialog(this, "Deseja mesmo excluir o usuário '" + login + "'?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                try {
                    dao.deletar(id);
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Usuário removido com sucesso.");
                } catch (DAOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir usuário:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Evento de Editar
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um usuário para editar.");
                return;
            }
            
            String id = (String) tableModel.getValueAt(row, 0);
            if (usuarioLogado.getId().equals(id)) {
                JOptionPane.showMessageDialog(this, "Você não pode editar a si mesmo por aqui.");
                return;
            }
            
            try {
                Usuario u = null;
                for (Usuario user : dao.listarTodos()) {
                    if (user.getId().equals(id)) {
                        u = user;
                        break;
                    }
                }
                if (u == null) return;
                
                JTextField txtEditLogin = new JTextField(u.getLogin());
                JPasswordField txtEditSenha = new JPasswordField(u.getSenha());
                JComboBox<String> cbEditTipo = new JComboBox<>(new String[]{"PORTEIRO", "SINDICO"});
                cbEditTipo.setSelectedItem(u.getTipo());
                JTextField txtEditCpf = new JTextField(u.getCpf());
                
                Object[] message = {
                    "Login:", txtEditLogin,
                    "Senha:", txtEditSenha,
                    "Tipo:", cbEditTipo,
                    "CPF:", txtEditCpf
                };
                
                int option = JOptionPane.showConfirmDialog(this, message, "Editar Usuário", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String nLogin = txtEditLogin.getText().trim();
                    String nSenha = new String(txtEditSenha.getPassword()).trim();
                    String nTipo = (String) cbEditTipo.getSelectedItem();
                    String nCpf = txtEditCpf.getText().trim();
                    
                    if (nLogin.isEmpty() || nSenha.isEmpty() || nCpf.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                        return;
                    }
                    if (!ValidadorCPF.isCPF(nCpf)) {
                        JOptionPane.showMessageDialog(this, "CPF inválido.");
                        return;
                    }
                    
                    String papel = portaria.database.DatabaseHelper.verificarPapelCpf(nCpf);
                    if (papel != null && !nCpf.replaceAll("[^0-9]", "").equals(u.getCpf().replaceAll("[^0-9]", ""))) {
                         JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado para outro " + papel + ".");
                         return;
                    }
                    
                    u.setLogin(nLogin);
                    u.setSenha(nSenha);
                    u.setTipo(nTipo);
                    u.setCpf(nCpf);
                    dao.atualizar(u);
                    
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Usuário editado com sucesso!");
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }

    private JPanel criarPainelGerenciarMoradores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("Cadastro de Moradores e Apartamentos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Formulário Esquerda
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Morador"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtNome = new JTextField(15);
        JTextField txtCpf = new JTextField(15);
        JTextField txtLogin = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);
        JComboBox<Integer> cbTorre = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        JComboBox<Integer> cbBloco = new JComboBox<>(new Integer[]{1, 2});
        JComboBox<Integer> cbAndar = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        JComboBox<Integer> cbNumero = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
        
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; formPanel.add(txtNome, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; formPanel.add(txtCpf, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1; formPanel.add(txtLogin, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; formPanel.add(txtSenha, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Torre:"), gbc);
        gbc.gridx = 1; formPanel.add(cbTorre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Bloco:"), gbc);
        gbc.gridx = 1; formPanel.add(cbBloco, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; formPanel.add(new JLabel("Andar:"), gbc);
        gbc.gridx = 1; formPanel.add(cbAndar, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; formPanel.add(new JLabel("Número (Apto):"), gbc);
        gbc.gridx = 1; formPanel.add(cbNumero, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        JButton btnSalvar = new JButton("Cadastrar Morador");
        formPanel.add(btnSalvar, gbc);
        
        panel.add(formPanel, BorderLayout.WEST);
        
        // Tabela Direita
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Moradores Cadastrados"));
        
        String[] colunas = {"ID", "Nome", "CPF", "Login", "Apartamento (ID)"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnEditar = new JButton("Editar Selecionado");
        tablePanel.add(btnEditar, BorderLayout.SOUTH);
        
        panel.add(tablePanel, BorderLayout.CENTER);
        
        MoradorDAO moradorDAO = new MoradorDAO();
        ApartamentoDAO aptoDAO = new ApartamentoDAO();
        
        Runnable recarregarTabela = () -> {
            tableModel.setRowCount(0);
            try {
                java.util.List<Morador> moradores = moradorDAO.listarTodos();
                for (Morador m : moradores) {
                    tableModel.addRow(new Object[]{m.getId(), m.getNome(), m.getCpf(), m.getLogin(), m.getApartamentoId()});
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar moradores:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
        recarregarTabela.run();
        
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            String login = txtLogin.getText().trim();
            String senha = new String(txtSenha.getPassword()).trim();
            int torre = (int) cbTorre.getSelectedItem();
            int bloco = (int) cbBloco.getSelectedItem();
            int andar = (int) cbAndar.getSelectedItem();
            int numero = (int) cbNumero.getSelectedItem();
            
            if (nome.isEmpty() || cpf.isEmpty() || login.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos do morador.");
                return;
            }
            
            if (!ValidadorCPF.isCPF(cpf)) {
                JOptionPane.showMessageDialog(this, "O CPF informado é inválido.");
                return;
            }
            
            String papel = portaria.database.DatabaseHelper.verificarPapelCpf(cpf);
            if (papel != null) {
                JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado no sistema como " + papel + ".");
                return;
            }
            
            try {
                // Primeiro insere o Apartamento e pega o ID
                Apartamento apto = new Apartamento(0, torre, bloco, andar, numero);
                int aptoId = aptoDAO.inserir(apto);
                
                if (aptoId == -1) {
                    JOptionPane.showMessageDialog(this, "Erro ao criar apartamento.");
                    return;
                }
                
                // Depois insere o Morador com o ID do apartamento
                Morador novo = new Morador(0, nome, cpf, login, senha, aptoId);
                moradorDAO.inserir(novo);
                
                txtNome.setText(""); txtCpf.setText(""); txtLogin.setText(""); txtSenha.setText("");
                recarregarTabela.run();
                JOptionPane.showMessageDialog(this, "Morador cadastrado com sucesso!");
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar no banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Evento de Editar
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um morador para editar.");
                return;
            }
            
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                Morador m = null;
                for (Morador mor : moradorDAO.listarTodos()) {
                    if (mor.getId() == id) {
                        m = mor;
                        break;
                    }
                }
                if (m == null) return;
                
                JTextField txtEditNome = new JTextField(m.getNome());
                JTextField txtEditCpf = new JTextField(m.getCpf());
                JTextField txtEditLogin = new JTextField(m.getLogin());
                JPasswordField txtEditSenha = new JPasswordField(m.getSenha());
                
                Apartamento apto = aptoDAO.buscarPorId(m.getApartamentoId());
                JComboBox<Integer> cbEditTorre = new JComboBox<>(new Integer[]{1, 2, 3, 4});
                JComboBox<Integer> cbEditBloco = new JComboBox<>(new Integer[]{1, 2});
                JComboBox<Integer> cbEditAndar = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                JComboBox<Integer> cbEditNumero = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
                if (apto != null) {
                    cbEditTorre.setSelectedItem(apto.getTorre());
                    cbEditBloco.setSelectedItem(apto.getBloco());
                    cbEditAndar.setSelectedItem(apto.getAndar());
                    cbEditNumero.setSelectedItem(apto.getNumero());
                }

                Object[] message = {
                    "Nome:", txtEditNome,
                    "CPF:", txtEditCpf,
                    "Login:", txtEditLogin,
                    "Senha:", txtEditSenha,
                    "Torre:", cbEditTorre,
                    "Bloco:", cbEditBloco,
                    "Andar:", cbEditAndar,
                    "Número:", cbEditNumero
                };
                
                int option = JOptionPane.showConfirmDialog(this, message, "Editar Morador", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String nNome = txtEditNome.getText().trim();
                    String nCpf = txtEditCpf.getText().trim();
                    String nLogin = txtEditLogin.getText().trim();
                    String nSenha = new String(txtEditSenha.getPassword()).trim();
                    
                    if (nNome.isEmpty() || nCpf.isEmpty() || nLogin.isEmpty() || nSenha.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Preencha todos os campos do morador.");
                        return;
                    }
                    if (!ValidadorCPF.isCPF(nCpf)) {
                        JOptionPane.showMessageDialog(this, "CPF inválido.");
                        return;
                    }
                    
                    String papel = portaria.database.DatabaseHelper.verificarPapelCpf(nCpf);
                    if (papel != null && !nCpf.replaceAll("[^0-9]", "").equals(m.getCpf().replaceAll("[^0-9]", ""))) {
                         JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado para outro " + papel + ".");
                         return;
                    }
                    
                    // Atualiza os dados físicos do apartamento
                    if (apto != null) {
                        apto.setTorre((int) cbEditTorre.getSelectedItem());
                        apto.setBloco((int) cbEditBloco.getSelectedItem());
                        apto.setAndar((int) cbEditAndar.getSelectedItem());
                        apto.setNumero((int) cbEditNumero.getSelectedItem());
                        aptoDAO.atualizar(apto);
                    }
                    
                    m.setNome(nNome);
                    m.setCpf(nCpf);
                    m.setLogin(nLogin);
                    m.setSenha(nSenha);
                    moradorDAO.atualizar(m);
                    
                    recarregarTabela.run();
                    JOptionPane.showMessageDialog(this, "Morador editado com sucesso!");
                }
            } catch (DAOException ex) {
                JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }
}

