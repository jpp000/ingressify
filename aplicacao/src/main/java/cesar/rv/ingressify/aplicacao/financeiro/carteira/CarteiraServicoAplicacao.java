package cesar.rv.ingressify.aplicacao.financeiro.carteira;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pontos.PontosServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

/**
 * Casos de uso da carteira do usuário: recarga (entrada de fundos) e saque (retirada),
 * cada operação registrando a transação correspondente no extrato.
 *
 * Programa de pontos de fidelidade: cada recarga credita {@link #PONTOS_POR_REAL} ponto
 * por real (arredondado para baixo). Acumulados {@link #PONTOS_POR_BLOCO} pontos, o usuário
 * pode resgatá-los em saldo, à razão de {@link #REAIS_POR_BLOCO} reais por bloco.
 */
public class CarteiraServicoAplicacao {

	private static final int PONTOS_POR_REAL = 1;
	private static final int PONTOS_POR_BLOCO = 1000;
	private static final int REAIS_POR_BLOCO = 10;

	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;
	private final PontosServico pontosServico;

	public CarteiraServicoAplicacao(SaldoServico saldoServico, TransacaoServico transacaoServico,
			PontosServico pontosServico) {
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(pontosServico, "pontosServico");
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
		this.pontosServico = pontosServico;
	}

	public Saldo obterSaldo(UsuarioId usuarioId) {
		return saldoServico.obter(usuarioId);
	}

	public int obterPontos(UsuarioId usuarioId) {
		return pontosServico.obter(usuarioId).getQuantidade();
	}

	public Saldo recarregar(UsuarioId usuarioId, BigDecimal valor) {
		Dinheiro quantia = validarValor(valor);
		saldoServico.creditar(usuarioId, quantia);
		registrar(usuarioId, TipoTransacao.RECARGA, quantia);
		// Fidelidade: 1 ponto por real recarregado (arredondado para baixo).
		int pontosGanhos = quantia.getValor().intValue() * PONTOS_POR_REAL;
		if (pontosGanhos > 0) {
			pontosServico.adicionar(usuarioId, pontosGanhos);
		}
		return saldoServico.obter(usuarioId);
	}

	public Saldo sacar(UsuarioId usuarioId, BigDecimal valor) {
		Dinheiro quantia = validarValor(valor);
		saldoServico.debitar(usuarioId, quantia);
		registrar(usuarioId, TipoTransacao.SAQUE, quantia);
		return saldoServico.obter(usuarioId);
	}

	/**
	 * Resgata o máximo possível de pontos em blocos de {@link #PONTOS_POR_BLOCO},
	 * creditando {@link #REAIS_POR_BLOCO} reais por bloco no saldo. A sobra (< 1 bloco)
	 * permanece acumulada. Lança IllegalStateException se não houver ao menos um bloco.
	 */
	public ResultadoResgate resgatarPontos(UsuarioId usuarioId) {
		int pontos = pontosServico.obter(usuarioId).getQuantidade();
		int blocos = pontos / PONTOS_POR_BLOCO;
		if (blocos == 0) {
			throw new IllegalStateException(
					"pontos insuficientes para resgate (mínimo de " + PONTOS_POR_BLOCO + " pontos)");
		}
		int pontosResgatados = blocos * PONTOS_POR_BLOCO;
		Dinheiro credito = new Dinheiro(BigDecimal.valueOf((long) blocos * REAIS_POR_BLOCO));

		pontosServico.remover(usuarioId, pontosResgatados);
		saldoServico.creditar(usuarioId, credito);
		registrar(usuarioId, TipoTransacao.RESGATE_PONTOS, credito);

		return new ResultadoResgate(saldoServico.obter(usuarioId), pontos - pontosResgatados,
				pontosResgatados, credito);
	}

	private Dinheiro validarValor(BigDecimal valor) {
		Validate.notNull(valor, "valor");
		Validate.isTrue(valor.signum() > 0, "valor deve ser positivo");
		return new Dinheiro(valor);
	}

	private void registrar(UsuarioId usuarioId, TipoTransacao tipo, Dinheiro quantia) {
		transacaoServico.registrar(new Transacao(usuarioId, tipo, quantia, LocalDateTime.now(), UUID.randomUUID()));
	}
}
