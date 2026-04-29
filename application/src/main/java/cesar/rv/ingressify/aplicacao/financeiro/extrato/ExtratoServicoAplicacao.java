package cesar.rv.ingressify.aplicacao.financeiro.extrato;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class ExtratoServicoAplicacao {

	private final TransacaoServico transacaoServico;
	private final SaldoServico saldoServico;

	public ExtratoServicoAplicacao(TransacaoServico transacaoServico, SaldoServico saldoServico) {
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(saldoServico, "saldoServico");
		this.transacaoServico = transacaoServico;
		this.saldoServico = saldoServico;
	}

	public ExtratoResumo obterExtrato(UsuarioId usuarioId) {
		Dinheiro saldo = saldoServico.obter(usuarioId).getValor();
		List<MovimentacaoResumo> movs = new ArrayList<>();
		for (Transacao t : transacaoServico.extratoPorUsuario(usuarioId)) {
			movs.add(new MovimentacaoResumo() {
				@Override
				public LocalDateTime getData() {
					return t.getData();
				}

				@Override
				public TipoTransacao getTipo() {
					return t.getTipo();
				}

				@Override
				public Dinheiro getValor() {
					return t.getValor();
				}

				@Override
				public UUID getReferencia() {
					return t.getReferenciaExternaId();
				}
			});
		}
		return new ExtratoResumo() {
			@Override
			public Dinheiro getSaldo() {
				return saldo;
			}

			@Override
			public List<MovimentacaoResumo> getMovimentacoes() {
				return List.copyOf(movs);
			}
		};
	}
}
