package cesar.rv.ingressify.dominio.financeiro.transacao;

import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class TransacaoServico {

	private final TransacaoRepositorio repositorio;

	public TransacaoServico(TransacaoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public void registrar(Transacao transacao) {
		repositorio.salvar(transacao);
	}

	public List<Transacao> extratoPorUsuario(UsuarioId usuario) {
		return repositorio.pesquisarPorUsuarioOrdenadoDesc(usuario);
	}
}
