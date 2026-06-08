package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.Objects;

public final class AssentoId {

    private final int id;

    public AssentoId(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssentoId other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "AssentoId(" + id + ")"; }
}
