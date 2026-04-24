package cesar.rv.ingressify.aplicacao.financeiro.pagamento;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;

@Service
public class PagamentoServicoAplicacao {

	private final PagamentoRepositorioAplicacao repositorio;

	public PagamentoServicoAplicacao(PagamentoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<PagamentoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public PagamentoResumo obterResumo(PagamentoId id) {
		return repositorio.obterResumo(id);
	}
}
