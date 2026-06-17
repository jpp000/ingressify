package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SolicitacaoReembolsoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.SolicitacaoReembolsoSpringDataRepository;

import static java.util.List.of;

@Repository
public class SolicitacaoReembolsoRepositorioPersistencia implements SolicitacaoReembolsoRepositorio {

	private final SolicitacaoReembolsoSpringDataRepository jpa;

	public SolicitacaoReembolsoRepositorioPersistencia(SolicitacaoReembolsoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(SolicitacaoReembolso solicitacao) {
		SolicitacaoReembolsoJpa saved = jpa.save(SolicitacaoReembolsoJpa.fromDomain(solicitacao));
		if (solicitacao.getId() == null) {
			solicitacao.atribuirId(new SolicitacaoReembolsoId(saved.getId()));
		}
	}

	@Override
	public SolicitacaoReembolso obter(SolicitacaoReembolsoId id) {
		return jpa.findById(id.getId())
				.map(SolicitacaoReembolsoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("SolicitacaoReembolso não encontrada: " + id.getId()));
	}

	@Override
	public Optional<SolicitacaoReembolso> pesquisarAtivaPorIngresso(IngressoId ingressoId) {
		return jpa.findByIngressoIdAndStatusIn(ingressoId.getId(),
				of(StatusSolicitacaoReembolso.PENDENTE, StatusSolicitacaoReembolso.EM_ANALISE))
				.map(SolicitacaoReembolsoJpa::toDomain);
	}

	@Override
	public List<SolicitacaoReembolso> pesquisarPorSolicitante(UsuarioId solicitanteId) {
		return jpa.findBySolicitanteId(solicitanteId.getId()).stream()
				.map(SolicitacaoReembolsoJpa::toDomain)
				.toList();
	}

	@Override
	public List<SolicitacaoReembolso> listarTodas() {
		return jpa.findAllByOrderByCriadaEmDesc().stream()
				.map(SolicitacaoReembolsoJpa::toDomain)
				.toList();
	}
}
