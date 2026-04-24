package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@Service
public class IngressoServicoAplicacao {

	private final IngressoRepositorioAplicacao repositorio;

	public IngressoServicoAplicacao(IngressoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<IngressoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<IngressoResumo> pesquisarResumosPorProprietario(UsuarioId usuario) {
		return repositorio.pesquisarResumosPorProprietario(usuario);
	}

	public IngressoResumoExpandido pesquisarResumoExpandido(IngressoId id) {
		return repositorio.pesquisarResumoExpandido(id);
	}
}
