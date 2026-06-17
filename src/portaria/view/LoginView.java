package portaria.view;

import portaria.dao.UsuarioDAO;
import portaria.model.Usuario;
import portaria.exception.DAOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private UsuarioDAO usuarioDAO;

    public LoginView() {
        usuarioDAO = new UsuarioDAO();
        
        setTitle("PortariaSegura - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("Usuário:"));
        txtLogin = new JTextField();
        panel.add(txtLogin);
        
        panel.add(new JLabel("Senha:"));
        txtSenha = new JPasswordField();
        panel.add(txtSenha);
        
        panel.add(new JLabel("")); // espaco vazio
        btnEntrar = new JButton("Entrar");
        panel.add(btnEntrar);
        
        add(panel);
        
        btnEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fazerLogin();
            }
        });
    }

    private void fazerLogin() {
        String login = txtLogin.getText();
        String senha = new String(txtSenha.getPassword());
        
        if(login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
            return;
        }
        
        try {
            Usuario u = usuarioDAO.autenticar(login, senha);
            if (u != null) {
                JOptionPane.showMessageDialog(this, "Bem-vindo(a), " + u.getLogin() + "!");
                MainView mainView = new MainView(u);
                mainView.setVisible(true);
                this.dispose();
                return;
            }
            
            portaria.dao.MoradorDAO moradorDAO = new portaria.dao.MoradorDAO();
            portaria.model.Morador m = moradorDAO.autenticar(login, senha);
            if (m != null) {
                JOptionPane.showMessageDialog(this, "Bem-vindo(a), " + m.getNome() + "!");
                MoradorMainView moradorView = new MoradorMainView(m);
                moradorView.setVisible(true);
                this.dispose();
                return;
            }

            JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos.");
        } catch (DAOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar com o banco de dados:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }
}

