package br.com.reservadaaldeia.portaria.model;

public class Encomenda {
    private String id;
    private int moradorId;
    private String entregador;
    private String recebedor;
    private String dataRecebimento;
    private String status; // PENDENTE ou ENTREGUE

    public Encomenda() {}

    public Encomenda(String id, int moradorId, String entregador, String recebedor, String dataRecebimento, String status) {
        this.id = id;
        this.moradorId = moradorId;
        this.entregador = entregador;
        this.recebedor = recebedor;
        this.dataRecebimento = dataRecebimento;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getMoradorId() { return moradorId; }
    public void setMoradorId(int moradorId) { this.moradorId = moradorId; }

    public String getEntregador() { return entregador; }
    public void setEntregador(String entregador) { this.entregador = entregador; }

    public String getRecebedor() { return recebedor; }
    public void setRecebedor(String recebedor) { this.recebedor = recebedor; }

    public String getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(String dataRecebimento) { this.dataRecebimento = dataRecebimento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
