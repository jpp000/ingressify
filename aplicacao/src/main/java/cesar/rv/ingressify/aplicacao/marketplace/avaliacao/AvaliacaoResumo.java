package cesar.rv.ingressify.aplicacao.marketplace.avaliacao;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;

public interface AvaliacaoResumo {

	AvaliacaoId getId();

	int getNota();

	String getComentario();

	String getRespostaOrganizador();

	LocalDateTime getCriadaEm();
}
