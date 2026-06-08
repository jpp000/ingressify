package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class Assento {

    private AssentoId id;
    private final MapaAssentosId mapaId;
    private final EventoId eventoId;
    private final String secao;
    private final String codigo;
    private final TipoAssento tipo;
    private BigDecimal preco;
    private StatusAssento status;
    private UsuarioId reservadoPor;
    private LocalDateTime reservadoAte;

    public Assento(MapaAssentosId mapaId, EventoId eventoId, String secao,
            String codigo, TipoAssento tipo, BigDecimal preco) {
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(eventoId, "eventoId");
        Validate.notBlank(secao, "secao");
        Validate.notBlank(codigo, "codigo");
        Validate.notNull(tipo, "tipo");
        Validate.notNull(preco, "preco");
        this.mapaId = mapaId;
        this.eventoId = eventoId;
        this.secao = secao;
        this.codigo = codigo;
        this.tipo = tipo;
        this.preco = preco;
        this.status = tipo == TipoAssento.BLOQUEADO ? StatusAssento.BLOQUEADO : StatusAssento.DISPONIVEL;
    }

    public Assento(AssentoId id, MapaAssentosId mapaId, EventoId eventoId, String secao,
            String codigo, TipoAssento tipo, BigDecimal preco, StatusAssento status,
            UsuarioId reservadoPor, LocalDateTime reservadoAte) {
        Validate.notNull(id, "id");
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(eventoId, "eventoId");
        Validate.notBlank(secao, "secao");
        Validate.notBlank(codigo, "codigo");
        Validate.notNull(tipo, "tipo");
        Validate.notNull(preco, "preco");
        Validate.notNull(status, "status");
        this.id = id;
        this.mapaId = mapaId;
        this.eventoId = eventoId;
        this.secao = secao;
        this.codigo = codigo;
        this.tipo = tipo;
        this.preco = preco;
        this.status = status;
        this.reservadoPor = reservadoPor;
        this.reservadoAte = reservadoAte;
    }

    public void atribuirId(AssentoId novoId) {
        Validate.notNull(novoId, "novoId");
        this.id = novoId;
    }

    public void reservar(UsuarioId usuarioId, int minutosReserva) {
        Validate.notNull(usuarioId, "usuarioId");
        if (status != StatusAssento.DISPONIVEL) {
            throw new IllegalStateException("assento " + codigo + " não está disponível");
        }
        this.status = StatusAssento.RESERVADO;
        this.reservadoPor = usuarioId;
        this.reservadoAte = LocalDateTime.now().plusMinutes(minutosReserva);
    }

    public void liberarReserva() {
        if (status != StatusAssento.RESERVADO) {
            throw new IllegalStateException("assento não está reservado");
        }
        this.status = StatusAssento.DISPONIVEL;
        this.reservadoPor = null;
        this.reservadoAte = null;
    }

    public void confirmarVenda() {
        if (status != StatusAssento.RESERVADO) {
            throw new IllegalStateException("assento deve estar reservado para confirmar venda");
        }
        this.status = StatusAssento.VENDIDO;
        this.reservadoAte = null;
    }

    public boolean reservaExpirada() {
        return status == StatusAssento.RESERVADO
                && reservadoAte != null
                && LocalDateTime.now().isAfter(reservadoAte);
    }

    public AssentoId getId() { return id; }
    public MapaAssentosId getMapaId() { return mapaId; }
    public EventoId getEventoId() { return eventoId; }
    public String getSecao() { return secao; }
    public String getCodigo() { return codigo; }
    public TipoAssento getTipo() { return tipo; }
    public BigDecimal getPreco() { return preco; }
    public StatusAssento getStatus() { return status; }
    public UsuarioId getReservadoPor() { return reservadoPor; }
    public LocalDateTime getReservadoAte() { return reservadoAte; }
}
