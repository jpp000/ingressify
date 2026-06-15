package cesar.rv.ingressify.aplicacao.financeiro.carteira;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

/**
 * Casos de uso da carteira do usuário: recarga (entrada de fundos) e saque (retirada),
 * cada operação registrando a transação correspondente no extrato.
 */
public class CarteiraServicoAplicacao {

	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;

	public CarteiraServicoAplicacao(SaldoServico saldoServico, TransacaoServico transacaoServico) {
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
	}

	public Saldo obterSaldo(UsuarioId usuarioId) {
		return saldoServico.obter(usuarioId);
	}

	public Saldo recarregar(UsuarioId usuarioId, BigDecimal valor) {
		Dinheiro quantia = validarValor(valor);
		saldoServico.creditar(usuarioId, quantia);
		registrar(usuarioId, TipoTransacao.RECARGA, quantia);
		return saldoServico.obter(usuarioId);
	}

	public Saldo sacar(UsuarioId usuarioId, BigDecimal valor) {
		Dinheiro quantia = validarValor(valor);
		saldoServico.debitar(usuarioId, quantia);
		registrar(usuarioId, TipoTransacao.SAQUE, quantia);
		return saldoServico.obter(usuarioId);
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
