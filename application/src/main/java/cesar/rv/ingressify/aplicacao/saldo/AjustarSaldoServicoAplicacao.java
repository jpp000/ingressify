package cesar.rv.ingressify.aplicacao.saldo;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Service
public class AjustarSaldoServicoAplicacao {

	private final SaldoServico saldoServico;
	private final TransacaoRepositorio transacaoRepositorio;

	public AjustarSaldoServicoAplicacao(SaldoServico saldoServico, TransacaoRepositorio transacaoRepositorio) {
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoRepositorio, "transacaoRepositorio");
		this.saldoServico = saldoServico;
		this.transacaoRepositorio = transacaoRepositorio;
	}

	@Transactional
	public void ajustar(UsuarioId usuario, Dinheiro novoValor) {
		Validate.notNull(usuario, "usuario");
		Validate.notNull(novoValor, "novoValor");
		saldoServico.ajustar(usuario, novoValor);
		transacaoRepositorio.salvar(new Transacao(usuario, TipoTransacao.AJUSTE_SALDO, novoValor, LocalDateTime.now(),
				UUID.randomUUID()));
	}
}
