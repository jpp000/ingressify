package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;

public class DenunciaServico {

	private final DenunciaRepositorio repositorio;

	public DenunciaServico(DenunciaRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Denuncia registrar(AnuncioRevendaId anuncioId, UsuarioId denuncianteId, MotivoDenuncia motivo,
			String descricao, LocalDateTime agora) {
		if (repositorio.existePorAnuncioEDenunciante(anuncioId, denuncianteId)) {
			throw new IllegalStateException("denúncia duplicada para o mesmo anúncio");
		}
		Denuncia d = new Denuncia(anuncioId, denuncianteId, motivo, descricao, agora);
		repositorio.salvar(d);
		return d;
	}

	public void decidir(DenunciaId id, DecisaoModeracao decisao, LocalDateTime agora) {
		Denuncia d = repositorio.obter(id);
		d.decidir(decisao, agora);
		repositorio.salvar(d);
	}

	public Denuncia obter(DenunciaId id) {
		return repositorio.obter(id);
	}
}
