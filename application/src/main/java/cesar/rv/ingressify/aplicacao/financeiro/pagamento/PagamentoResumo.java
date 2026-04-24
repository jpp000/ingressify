package cesar.rv.ingressify.aplicacao.financeiro.pagamento;

import java.math.BigDecimal;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;

public interface PagamentoResumo {

	int getId();

	BigDecimal getValor();

	int getCompradorId();

	Integer getVendedorId();

	TipoOperacao getTipo();

	StatusPagamento getStatus();

	UUID getCorrelacaoId();
}
