package portaria.model;

public class Apartamento {
    private int id;
    private int torre;
    private int bloco;
    private int andar;
    private int numero;

    public Apartamento() {}

    public Apartamento(int id, int torre, int bloco, int andar, int numero) {
        this.id = id;
        this.torre = torre;
        this.bloco = bloco;
        this.andar = andar;
        this.numero = numero;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTorre() { return torre; }
    public void setTorre(int torre) { this.torre = torre; }

    public int getBloco() { return bloco; }
    public void setBloco(int bloco) { this.bloco = bloco; }

    public int getAndar() { return andar; }
    public void setAndar(int andar) { this.andar = andar; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    @Override
    public String toString() {
        return "T" + torre + "-B" + bloco + "-A" + andar + "-N" + numero;
    }
}

