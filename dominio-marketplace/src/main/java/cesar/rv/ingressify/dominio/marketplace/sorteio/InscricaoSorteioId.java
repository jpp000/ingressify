package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.util.Objects;

public final class InscricaoSorteioId {

    private final int id;

    public InscricaoSorteioId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InscricaoSorteioId other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InscricaoSorteioId(" + id + ")";
    }
}
