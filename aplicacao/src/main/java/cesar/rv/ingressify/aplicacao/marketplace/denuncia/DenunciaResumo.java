package cesar.rv.ingressify.aplicacao.marketplace.denuncia;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;

public interface DenunciaResumo {

	DenunciaId getId();

	AnuncioRevendaId getAnuncioId();

	UsuarioId getDenuncianteId();

	MotivoDenuncia getMotivo();

	String getDescricao();

	StatusDenuncia getStatus();

	LocalDateTime getCriadaEm();
}
