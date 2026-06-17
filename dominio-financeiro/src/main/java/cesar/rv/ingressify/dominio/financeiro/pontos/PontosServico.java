package cesar.rv.ingressify.dominio.financeiro.pontos;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class PontosServico {

	private final PontosRepositorio repositorio;

	public PontosServico(PontosRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Pontos obter(UsuarioId usuario) {
		return repositorio.obter(usuario);
	}

	public void adicionar(UsuarioId usuario, int pontos) {
		Pontos p = repositorio.obter(usuario);
		p.adicionar(pontos);
		repositorio.salvar(p);
	}

	public void remover(UsuarioId usuario, int pontos) {
		Pontos p = repositorio.obter(usuario);
		p.remover(pontos);
		repositorio.salvar(p);
	}
}
