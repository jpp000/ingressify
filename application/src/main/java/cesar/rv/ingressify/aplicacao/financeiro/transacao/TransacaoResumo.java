package cesar.rv.ingressify.aplicacao.financeiro.transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;

public interface TransacaoResumo {

	int getId();

	int getUsuarioId();

	TipoTransacao getTipo();

	BigDecimal getValor();

	LocalDateTime getData();

	UUID getReferenciaExternaId();

	default String getDescricao() {
		return getTipo().name();
	}
}
