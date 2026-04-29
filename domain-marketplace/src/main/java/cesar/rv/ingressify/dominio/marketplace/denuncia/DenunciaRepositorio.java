package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;

public interface DenunciaRepositorio {

	Denuncia obter(DenunciaId id);

	void salvar(Denuncia denuncia);

	boolean existePorAnuncioEDenunciante(AnuncioRevendaId anuncioId, UsuarioId denuncianteId);

	List<Denuncia> pesquisarPendentes();
}
