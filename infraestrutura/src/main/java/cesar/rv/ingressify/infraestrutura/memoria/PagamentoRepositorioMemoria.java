package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoRepositorio;

public class PagamentoRepositorioMemoria implements PagamentoRepositorio {

	private final Map<Integer, Pagamento> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(Pagamento pagamento) {
		if (pagamento.getId() == null) {
			pagamento.atribuirId(new PagamentoId(sequencia.getAndIncrement()));
		}
		store.put(pagamento.getId().getId(), pagamento);
	}

	@Override
	public Pagamento obter(PagamentoId id) {
		Pagamento p = store.get(id.getId());
		if (p == null) {
			throw new IllegalArgumentException("Pagamento não encontrado: " + id.getId());
		}
		return p;
	}

	@Override
	public Optional<Pagamento> obterPorCorrelacao(UUID correlacaoId) {
		return store.values().stream()
				.filter(p -> p.getCorrelacaoId().equals(correlacaoId))
				.findFirst();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
