package cesar.rv.ingressify.aplicacao.financeiro.extrato;

import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;

public interface MovimentacaoResumo {

	LocalDateTime getData();

	TipoTransacao getTipo();

	Dinheiro getValor();

	UUID getReferencia();
}
