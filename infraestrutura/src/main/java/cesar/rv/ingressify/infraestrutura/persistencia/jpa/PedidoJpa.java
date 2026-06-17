package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Entity
@Table(name = "pedidos")
public class PedidoJpa {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "tipo_ingresso_id", nullable = false)
	private Integer tipoIngressoId;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(nullable = false)
	private int quantidade;

	@Column(name = "comprador_id", nullable = false)
	private Integer compradorId;

	@Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
	private BigDecimal valorTotal;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	protected PedidoJpa() {}

	public static PedidoJpa fromDomain(Pedido p) {
		var jpa = new PedidoJpa();
		jpa.id = p.getId();
		jpa.tipoIngressoId = p.getTipoIngressoId().getId();
		jpa.eventoId = p.getEventoId().getId();
		jpa.quantidade = p.getQuantidade();
		jpa.compradorId = p.getComprador().getId();
		jpa.valorTotal = p.getValorTotal().getValor();
		jpa.criadaEm = p.getCriadaEm();
		return jpa;
	}

	public Pedido toDomain() {
		return new Pedido(id, new TipoIngressoId(tipoIngressoId), new EventoId(eventoId),
				quantidade, new UsuarioId(compradorId), new Dinheiro(valorTotal), criadaEm);
	}

	public UUID getId() { return id; }
	public Integer getTipoIngressoId() { return tipoIngressoId; }
	public Integer getCompradorId() { return compradorId; }
}
