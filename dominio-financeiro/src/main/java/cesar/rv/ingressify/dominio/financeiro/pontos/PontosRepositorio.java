package cesar.rv.ingressify.dominio.financeiro.pontos;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface PontosRepositorio {

	void salvar(Pontos pontos);

	Pontos obter(UsuarioId usuario);
}
