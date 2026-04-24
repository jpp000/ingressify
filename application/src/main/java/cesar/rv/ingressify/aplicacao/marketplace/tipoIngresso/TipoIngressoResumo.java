package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.math.BigDecimal;

public interface TipoIngressoResumo {

	int getId();

	int getEventoId();

	String getNome();

	BigDecimal getPrecoValor();

	int getQuantidadeDisponivel();

	int getQuantidadeTotal();
}
