package cesar.rv.ingressify.aplicacao.marketplace.anuncio;

import java.math.BigDecimal;
import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;

public interface AnuncioResumo {

	int getId();

	UUID getIngressoId();

	int getVendedorId();

	BigDecimal getPrecoValor();

	Integer getCompradorReservadoId();

	StatusAnuncio getStatus();
}
