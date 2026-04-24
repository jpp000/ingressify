package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumoExpandido;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;

@Repository
public class IngressoRepositorioImpl implements IngressoRepositorio, IngressoRepositorioAplicacao {

	private final IngressoJpaRepository jpaRepository;
	private final EventoJpaRepository eventoJpaRepository;
	private final TipoIngressoJpaRepository tipoIngressoJpaRepository;
	private final JpaMapeador mapeador;

	public IngressoRepositorioImpl(IngressoJpaRepository jpaRepository, EventoJpaRepository eventoJpaRepository,
			TipoIngressoJpaRepository tipoIngressoJpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.eventoJpaRepository = eventoJpaRepository;
		this.tipoIngressoJpaRepository = tipoIngressoJpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(Ingresso ingresso) {
		IngressoJpa jpa = jpaRepository.findById(ingresso.getId().getId()).orElseGet(IngressoJpa::new);
		mapeador.preencherIngressoJpa(jpa, ingresso);
		jpaRepository.save(jpa);
	}

	@Override
	public Ingresso obter(IngressoId id) {
		return jpaRepository.findById(id.getId()).map(mapeador::paraIngresso)
				.orElseThrow(() -> new IllegalArgumentException("ingresso não encontrado"));
	}

	@Override
	public List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario) {
		return jpaRepository.findByProprietarioId(proprietario.getId()).stream().map(mapeador::paraIngresso).toList();
	}

	@Override
	public List<IngressoResumo> pesquisarResumos() {
		return jpaRepository.pesquisarResumos();
	}

	@Override
	public List<IngressoResumo> pesquisarResumosPorProprietario(UsuarioId usuario) {
		return jpaRepository.pesquisarResumosPorProprietario(usuario.getId());
	}

	@Override
	public IngressoResumoExpandido pesquisarResumoExpandido(IngressoId id) {
		IngressoJpa j = jpaRepository.findById(id.getId()).orElseThrow(() -> new IllegalArgumentException("ingresso não encontrado"));
		IngressoResumo base = jpaRepository.obterResumo(j.getId());
		EventoResumo evento = eventoJpaRepository.resumoPorId(j.getEventoId())
				.orElseThrow(() -> new IllegalStateException("evento não encontrado"));
		TipoIngressoResumo tipo = tipoIngressoJpaRepository.obterResumo(j.getTipoIngressoId());
		return new IngressoResumoExpandidoImpl(base, evento, tipo);
	}

	private record IngressoResumoExpandidoImpl(IngressoResumo base, EventoResumo evento, TipoIngressoResumo tipoIngresso)
			implements
				IngressoResumoExpandido {

		@Override
		public EventoResumo getEvento() {
			return evento;
		}

		@Override
		public TipoIngressoResumo getTipoIngresso() {
			return tipoIngresso;
		}

		@Override
		public java.util.UUID getId() {
			return base.getId();
		}

		@Override
		public int getTipoIngressoId() {
			return base.getTipoIngressoId();
		}

		@Override
		public int getEventoId() {
			return base.getEventoId();
		}

		@Override
		public int getProprietarioId() {
			return base.getProprietarioId();
		}

		@Override
		public cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso getStatus() {
			return base.getStatus();
		}
	}
}
