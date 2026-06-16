package br.com.reservadaaldeia.portaria.model;

public class Morador {
    private int id;
    private String nome;
    private String cpf;
    private String login;
    private String senha;
    private int apartamentoId;

    public Morador() {}

    public Morador(int id, String nome, String cpf, String login, String senha, int apartamentoId) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.login = login;
        this.senha = senha;
        this.apartamentoId = apartamentoId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getApartamentoId() { return apartamentoId; }
    public void setApartamentoId(int apartamentoId) { this.apartamentoId = apartamentoId; }
}
