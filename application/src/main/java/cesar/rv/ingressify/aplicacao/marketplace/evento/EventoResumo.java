package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

public interface EventoResumo {

	int getId();

	String getNome();

	LocalDateTime getDataHora();

	String getLocal();

	StatusEvento getStatus();

	int getCapacidade();
}
