package cesar.rv.ingressify.aplicacao.marketplace.catalogo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface EventoCatalogoResumo {

	EventoId getId();

	String getNome();

	LocalDateTime getDataHora();

	String getLocal();

	String getImagemCapaUrl();

	Dinheiro getPrecoMinimo();

	double getMediaAvaliacao();

	boolean isTemEstoquePrimario();

	boolean isTemRevendaDisponivel();
}
