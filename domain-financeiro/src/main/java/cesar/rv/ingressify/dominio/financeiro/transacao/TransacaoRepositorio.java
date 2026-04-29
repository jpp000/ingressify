package cesar.rv.ingressify.dominio.financeiro.transacao;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface TransacaoRepositorio {

	void salvar(Transacao transacao);

	List<Transacao> pesquisarPorUsuario(UsuarioId usuario);

	List<Transacao> pesquisarPorUsuarioOrdenadoDesc(UsuarioId usuario);
}
