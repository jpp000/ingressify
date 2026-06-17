package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;

public class SolicitacaoReembolsoRepositorioMemoria implements SolicitacaoReembolsoRepositorio {

	private final Map<Integer, SolicitacaoReembolso> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(SolicitacaoReembolso solicitacao) {
		if (solicitacao.getId() == null) {
			solicitacao.atribuirId(new SolicitacaoReembolsoId(sequencia.getAndIncrement()));
		}
		store.put(solicitacao.getId().getId(), solicitacao);
	}

	@Override
	public SolicitacaoReembolso obter(SolicitacaoReembolsoId id) {
		SolicitacaoReembolso s = store.get(id.getId());
		if (s == null) {
			throw new IllegalArgumentException("SolicitacaoReembolso não encontrada: " + id.getId());
		}
		return s;
	}

	@Override
	public Optional<SolicitacaoReembolso> pesquisarAtivaPorIngresso(IngressoId ingressoId) {
		return store.values().stream()
				.filter(s -> s.getIngressoId().equals(ingressoId)
						&& (s.getStatus() == StatusSolicitacaoReembolso.PENDENTE
								|| s.getStatus() == StatusSolicitacaoReembolso.EM_ANALISE))
				.findFirst();
	}

	@Override
	public List<SolicitacaoReembolso> pesquisarPorSolicitante(UsuarioId solicitanteId) {
		return store.values().stream()
				.filter(s -> s.getSolicitanteId().equals(solicitanteId))
				.toList();
	}

	@Override
	public List<SolicitacaoReembolso> listarTodas() {
		return store.values().stream()
				.sorted(java.util.Comparator.comparingInt(s -> s.getId().getId()))
				.toList();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
