package cesar.rv.ingressify.aplicacao.marketplace.anuncio;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;

@Service
public class AnuncioServicoAplicacao {

	private final AnuncioRepositorioAplicacao repositorio;

	public AnuncioServicoAplicacao(AnuncioRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<AnuncioResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public AnuncioResumoExpandido pesquisarResumoExpandido(AnuncioRevendaId id) {
		return repositorio.pesquisarResumoExpandido(id);
	}
}
