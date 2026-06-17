package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.cupom.Cupom;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomId;
import cesar.rv.ingressify.dominio.marketplace.cupom.TipoCupom;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@Entity
@Table(name = "cupons", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class CupomJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String codigo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoCupom tipo;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "valor_minimo", nullable = false, precision = 19, scale = 2)
	private BigDecimal valorMinimo;

	@Column(name = "limite_usos", nullable = false)
	private int limiteUsos;

	@Column(nullable = false)
	private int usos;

	@Column(name = "valido_de")
	private LocalDateTime validoDe;

	@Column(name = "valido_ate")
	private LocalDateTime validoAte;

	@Column(nullable = false)
	private boolean ativo;

	protected CupomJpa() {}

	public static CupomJpa fromDomain(Cupom c) {
		var jpa = new CupomJpa();
		if (c.getId() != null) jpa.id = c.getId().getId();
		jpa.codigo = c.getCodigo();
		jpa.tipo = c.getTipo();
		jpa.valor = c.getValor();
		jpa.eventoId = c.getEventoId().getId();
		jpa.valorMinimo = c.getValorMinimo().getValor();
		jpa.limiteUsos = c.getLimiteUsos();
		jpa.usos = c.getUsos();
		jpa.validoDe = c.getValidoDe();
		jpa.validoAte = c.getValidoAte();
		jpa.ativo = c.isAtivo();
		return jpa;
	}

	public Cupom toDomain() {
		return new Cupom(
				new CupomId(id), codigo, tipo, valor, new EventoId(eventoId), new Dinheiro(valorMinimo),
				limiteUsos, usos, validoDe, validoAte, ativo);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
}
