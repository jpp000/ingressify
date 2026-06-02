package cesar.rv.ingressify.aplicacao.marketplace.feed;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;

public interface PostagemResumo {

	PostagemId getId();

	String getTitulo();

	String getConteudo();

	String getImagemUrl();

	boolean isFixada();

	LocalDateTime getCriadaEm();

	UsuarioId getAutorId();
}
