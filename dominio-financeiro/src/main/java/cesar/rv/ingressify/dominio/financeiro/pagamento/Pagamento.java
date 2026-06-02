package cesar.rv.ingressify.dominio.financeiro.pagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Pagamento {

	public record Divisao(Dinheiro vendedor, Dinheiro plataforma) {
	}

	private PagamentoId id;
	private Dinheiro valor;
	private UsuarioId comprador;
	private UsuarioId vendedor;
	private TipoOperacao tipo;
	private StatusPagamento status;
	private UUID correlacaoId;

	public Pagamento(Dinheiro valor, UsuarioId comprador, UsuarioId vendedor, TipoOperacao tipo, UUID correlacaoId) {
		Validate.notNull(valor, "valor");
		Validate.notNull(comprador, "comprador");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(correlacaoId, "correlacaoId");
		this.valor = valor;
		this.comprador = comprador;
		this.vendedor = vendedor;
		this.tipo = tipo;
		this.status = StatusPagamento.PENDENTE;
		this.correlacaoId = correlacaoId;
	}

	public Pagamento(PagamentoId id, Dinheiro valor, UsuarioId comprador, UsuarioId vendedor, TipoOperacao tipo,
			StatusPagamento status, UUID correlacaoId) {
		Validate.notNull(id, "id");
		Validate.notNull(valor, "valor");
		Validate.notNull(comprador, "comprador");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(status, "status");
		Validate.notNull(correlacaoId, "correlacaoId");
		this.id = id;
		this.valor = valor;
		this.comprador = comprador;
		this.vendedor = vendedor;
		this.tipo = tipo;
		this.status = status;
		this.correlacaoId = correlacaoId;
	}

	public void atribuirId(PagamentoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void confirmar() {
		if (status != StatusPagamento.PENDENTE) {
			throw new IllegalStateException("somente pagamento PENDENTE pode ser confirmado");
		}
		this.status = StatusPagamento.CONFIRMADO;
	}

	public void rejeitar() {
		if (status != StatusPagamento.PENDENTE) {
			throw new IllegalStateException("somente pagamento PENDENTE pode ser rejeitado");
		}
		this.status = StatusPagamento.REJEITADO;
	}

	public Divisao dividir(BigDecimal percentualPlataforma) {
		Validate.notNull(percentualPlataforma, "percentualPlataforma");
		Validate.isTrue(percentualPlataforma.compareTo(BigDecimal.ZERO) >= 0
				&& percentualPlataforma.compareTo(BigDecimal.ONE) <= 0, "percentual entre 0 e 1");
		BigDecimal valorBd = valor.getValor();
		BigDecimal plataformaBd = valorBd.multiply(percentualPlataforma).setScale(2, RoundingMode.HALF_UP);
		BigDecimal vendedorBd = valorBd.subtract(plataformaBd).setScale(2, RoundingMode.HALF_UP);
		return new Divisao(new Dinheiro(vendedorBd), new Dinheiro(plataformaBd));
	}

	public PagamentoId getId() {
		return id;
	}

	public Dinheiro getValor() {
		return valor;
	}

	public UsuarioId getComprador() {
		return comprador;
	}

	public UsuarioId getVendedor() {
		return vendedor;
	}

	public TipoOperacao getTipo() {
		return tipo;
	}

	public StatusPagamento getStatus() {
		return status;
	}

	public UUID getCorrelacaoId() {
		return correlacaoId;
	}
}
