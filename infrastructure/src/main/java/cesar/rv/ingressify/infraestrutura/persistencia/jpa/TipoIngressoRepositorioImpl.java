package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

@Repository
public class TipoIngressoRepositorioImpl implements TipoIngressoRepositorio, TipoIngressoRepositorioAplicacao {

	private final TipoIngressoJpaRepository jpaRepository;
	private final JpaMapeador mapeador;

	public TipoIngressoRepositorioImpl(TipoIngressoJpaRepository jpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(TipoIngresso tipoIngresso) {
		TipoIngressoJpa jpa = tipoIngresso.getId() == null ? new TipoIngressoJpa()
				: jpaRepository.findById(tipoIngresso.getId().getId()).orElseGet(TipoIngressoJpa::new);
		mapeador.preencherTipoIngressoJpa(jpa, tipoIngresso);
		TipoIngressoJpa salvo = jpaRepository.save(jpa);
		if (tipoIngresso.getId() == null) {
			tipoIngresso.atribuirId(new TipoIngressoId(salvo.getId()));
		}
	}

	@Override
	public TipoIngresso obter(TipoIngressoId id) {
		return jpaRepository.findById(id.getId()).map(mapeador::paraTipoIngresso)
				.orElseThrow(() -> new IllegalArgumentException("tipo de ingresso não encontrado"));
	}

	@Override
	public List<TipoIngresso> pesquisarPorEvento(EventoId eventoId) {
		return jpaRepository.findByEventoId(eventoId.getId()).stream().map(mapeador::paraTipoIngresso).toList();
	}

	@Override
	public List<TipoIngressoResumo> pesquisarResumosPorEvento(EventoId eventoId) {
		return jpaRepository.pesquisarResumosPorEvento(eventoId.getId());
	}

	@Override
	public List<TipoIngressoResumo> pesquisarResumos() {
		return jpaRepository.pesquisarResumos();
	}

	@Override
	public TipoIngressoResumo obterResumo(TipoIngressoId id) {
		return jpaRepository.obterResumo(id.getId());
	}
}
