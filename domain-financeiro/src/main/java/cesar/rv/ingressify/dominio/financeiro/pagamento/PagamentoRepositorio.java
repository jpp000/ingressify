package cesar.rv.ingressify.dominio.financeiro.pagamento;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepositorio {

	void salvar(Pagamento pagamento);

	Pagamento obter(PagamentoId id);

	Optional<Pagamento> obterPorCorrelacao(UUID correlacaoId);
}

