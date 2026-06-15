package cesar.rv.ingressify.dominio.marketplace.cupom;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.cupom.estrategia.DescontoPercentualEstrategia;
import cesar.rv.ingressify.dominio.marketplace.cupom.estrategia.DescontoValorFixoEstrategia;
import cesar.rv.ingressify.dominio.marketplace.cupom.estrategia.EstrategiaDesconto;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

/**
 * Cupom de desconto vinculado a um evento. O cálculo do desconto é delegado a uma
 * {@link EstrategiaDesconto} (padrão Strategy) escolhida conforme o {@link TipoCupom}.
 */
public class Cupom {

	private CupomId id;
	private String codigo;
	private final TipoCupom tipo;
	private final BigDecimal valor;
	private final EventoId eventoId;
	private final Dinheiro valorMinimo;
	private final int limiteUsos;
	private int usos;
	private final LocalDateTime validoDe;
	private final LocalDateTime validoAte;
	private boolean ativo;

	public Cupom(String codigo, TipoCupom tipo, BigDecimal valor, EventoId eventoId, Dinheiro valorMinimo,
			int limiteUsos, LocalDateTime validoDe, LocalDateTime validoAte) {
		Validate.notBlank(codigo, "codigo");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(valor, "valor");
		Validate.notNull(eventoId, "eventoId");
		Validate.isTrue(limiteUsos >= 0, "limiteUsos deve ser >= 0");
		validarValor(tipo, valor);
		if (validoDe != null && validoAte != null) {
			Validate.isTrue(!validoAte.isBefore(validoDe), "validoAte deve ser igual ou após validoDe");
		}
		this.codigo = codigo.trim().toUpperCase();
		this.tipo = tipo;
		this.valor = valor;
		this.eventoId = eventoId;
		this.valorMinimo = valorMinimo != null ? valorMinimo : Dinheiro.ZERO;
		this.limiteUsos = limiteUsos;
		this.usos = 0;
		this.validoDe = validoDe;
		this.validoAte = validoAte;
		this.ativo = true;
	}

	public Cupom(CupomId id, String codigo, TipoCupom tipo, BigDecimal valor, EventoId eventoId, Dinheiro valorMinimo,
			int limiteUsos, int usos, LocalDateTime validoDe, LocalDateTime validoAte, boolean ativo) {
		Validate.notNull(id, "id");
		Validate.notBlank(codigo, "codigo");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(valor, "valor");
		Validate.notNull(eventoId, "eventoId");
		this.id = id;
		this.codigo = codigo;
		this.tipo = tipo;
		this.valor = valor;
		this.eventoId = eventoId;
		this.valorMinimo = valorMinimo != null ? valorMinimo : Dinheiro.ZERO;
		this.limiteUsos = limiteUsos;
		this.usos = usos;
		this.validoDe = validoDe;
		this.validoAte = validoAte;
		this.ativo = ativo;
	}

	private static void validarValor(TipoCupom tipo, BigDecimal valor) {
		if (tipo == TipoCupom.PERCENTUAL) {
			int p = valor.intValueExact();
			Validate.isTrue(p >= 1 && p <= 99, "percentual deve estar entre 1 e 99");
		} else {
			Validate.isTrue(valor.signum() > 0, "valor fixo deve ser > 0");
		}
	}

	public void atribuirId(CupomId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	/** Cria a estratégia de desconto correspondente ao tipo do cupom. */
	public EstrategiaDesconto estrategia() {
		return switch (tipo) {
			case PERCENTUAL -> new DescontoPercentualEstrategia(valor.intValueExact());
			case VALOR_FIXO -> new DescontoValorFixoEstrategia(new Dinheiro(valor));
		};
	}

	/** Verifica se o cupom pode ser aplicado, lançando exceção com o motivo caso contrário. */
	public void validarAplicavel(Dinheiro valorCompra, EventoId eventoCompra, LocalDateTime agora) {
		Validate.notNull(valorCompra, "valorCompra");
		Validate.notNull(eventoCompra, "eventoCompra");
		Validate.notNull(agora, "agora");
		if (!ativo) {
			throw new IllegalStateException("cupom inativo");
		}
		if (validoDe != null && agora.isBefore(validoDe)) {
			throw new IllegalStateException("cupom ainda não está vigente");
		}
		if (validoAte != null && agora.isAfter(validoAte)) {
			throw new IllegalStateException("cupom expirado");
		}
		if (atingiuLimite()) {
			throw new IllegalStateException("limite de usos do cupom atingido");
		}
		if (!eventoId.equals(eventoCompra)) {
			throw new IllegalStateException("cupom não é válido para este evento");
		}
		if (!valorCompra.maiorOuIgualA(valorMinimo)) {
			throw new IllegalStateException("valor mínimo para uso do cupom não atingido");
		}
	}

	/** Aplica o desconto ao valor informado (não altera o estado do cupom). */
	public Dinheiro aplicar(Dinheiro valorOriginal) {
		return estrategia().aplicar(valorOriginal);
	}

	/** Registra um uso do cupom, respeitando o limite configurado. */
	public void registrarUso() {
		if (atingiuLimite()) {
			throw new IllegalStateException("limite de usos do cupom atingido");
		}
		this.usos++;
	}

	public void desativar() {
		this.ativo = false;
	}

	public boolean atingiuLimite() {
		return limiteUsos > 0 && usos >= limiteUsos;
	}

	public CupomId getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public TipoCupom getTipo() {
		return tipo;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public Dinheiro getValorMinimo() {
		return valorMinimo;
	}

	public int getLimiteUsos() {
		return limiteUsos;
	}

	public int getUsos() {
		return usos;
	}

	public LocalDateTime getValidoDe() {
		return validoDe;
	}

	public LocalDateTime getValidoAte() {
		return validoAte;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
