package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.financeiro.pagamento.PagamentoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.financeiro.pagamento.PagamentoResumo;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoRepositorio;

@Repository
public class PagamentoRepositorioImpl implements PagamentoRepositorio, PagamentoRepositorioAplicacao {

	private final PagamentoJpaRepository jpaRepository;
	private final JpaMapeador mapeador;

	public PagamentoRepositorioImpl(PagamentoJpaRepository jpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(Pagamento pagamento) {
		PagamentoJpa jpa = pagamento.getId() == null ? new PagamentoJpa()
				: jpaRepository.findById(pagamento.getId().getId()).orElseGet(PagamentoJpa::new);
		mapeador.preencherPagamentoJpa(jpa, pagamento);
		PagamentoJpa salvo = jpaRepository.save(jpa);
		if (pagamento.getId() == null) {
			pagamento.atribuirId(new PagamentoId(salvo.getId()));
		}
	}

	@Override
	public Pagamento obter(PagamentoId id) {
		return jpaRepository.findById(id.getId()).map(mapeador::paraPagamento)
				.orElseThrow(() -> new IllegalArgumentException("pagamento não encontrado"));
	}

	@Override
	public Optional<Pagamento> obterPorCorrelacao(UUID correlacaoId) {
		return jpaRepository.findByCorrelacaoId(correlacaoId).map(mapeador::paraPagamento);
	}

	@Override
	public List<PagamentoResumo> pesquisarResumos() {
		return jpaRepository.pesquisarResumos();
	}

	@Override
	public PagamentoResumo obterResumo(PagamentoId id) {
		return jpaRepository.obterResumo(id.getId());
	}
}
