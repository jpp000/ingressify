package cesar.rv.ingressify.dominio.marketplace.ingresso;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface IngressoRepositorio {

	void salvar(Ingresso ingresso);

	Ingresso obter(IngressoId id);

	List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario);
}
