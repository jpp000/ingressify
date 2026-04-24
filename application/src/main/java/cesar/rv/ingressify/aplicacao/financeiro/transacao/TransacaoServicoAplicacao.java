package cesar.rv.ingressify.aplicacao.financeiro.transacao;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Service
public class TransacaoServicoAplicacao {

	private final TransacaoRepositorioAplicacao repositorio;

	public TransacaoServicoAplicacao(TransacaoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<TransacaoResumo> pesquisarHistorico(UsuarioId usuario) {
		return repositorio.pesquisarHistorico(usuario);
	}
}
