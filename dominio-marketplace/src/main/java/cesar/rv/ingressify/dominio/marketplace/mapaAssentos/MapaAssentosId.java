package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.Objects;

public final class MapaAssentosId {

    private final int id;

    public MapaAssentosId(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapaAssentosId other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "MapaAssentosId(" + id + ")"; }
}
