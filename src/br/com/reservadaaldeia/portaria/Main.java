package br.com.reservadaaldeia.portaria;

import br.com.reservadaaldeia.portaria.database.DatabaseHelper;
import br.com.reservadaaldeia.portaria.view.LoginView;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Inicializa o banco de dados (cria o arquivo sqlite e tabelas)
        DatabaseHelper.inicializarBanco();
        
        // Usa o look and feel padrão do sistema operacional (Swing básico)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginView login = new LoginView();
                login.setVisible(true);
            }
        });
    }
}
