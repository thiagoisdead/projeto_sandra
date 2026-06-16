package br.com.reservadaaldeia.portaria.model;

public class Usuario {
    private String id;
    private String login;
    private String senha;
    private String tipo;
    private String cpf;

    public Usuario() {}

    public Usuario(String id, String login, String senha, String tipo, String cpf) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.tipo = tipo;
        this.cpf = cpf;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
}
