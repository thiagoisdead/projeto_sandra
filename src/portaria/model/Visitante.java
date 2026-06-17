package portaria.model;

public class Visitante {
    private int id;
    private String nome;
    private String cpf;
    private int apartamentoId;
    private int moradorId;
    private String dataEntrada;
    private String dataSaida;

    public Visitante() {}

    public Visitante(int id, String nome, String cpf, int apartamentoId, int moradorId, String dataEntrada, String dataSaida) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.apartamentoId = apartamentoId;
        this.moradorId = moradorId;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public int getApartamentoId() { return apartamentoId; }
    public void setApartamentoId(int apartamentoId) { this.apartamentoId = apartamentoId; }

    public int getMoradorId() { return moradorId; }
    public void setMoradorId(int moradorId) { this.moradorId = moradorId; }

    public String getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(String dataEntrada) { this.dataEntrada = dataEntrada; }

    public String getDataSaida() { return dataSaida; }
    public void setDataSaida(String dataSaida) { this.dataSaida = dataSaida; }
}

