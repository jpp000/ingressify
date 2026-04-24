package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumoExpandido;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

@Repository
public class EventoRepositorioImpl implements EventoRepositorio, EventoRepositorioAplicacao {

	private final EventoJpaRepository jpaRepository;
	private final TipoIngressoJpaRepository tipoIngressoJpaRepository;
	private final JpaMapeador mapeador;

	public EventoRepositorioImpl(EventoJpaRepository jpaRepository, TipoIngressoJpaRepository tipoIngressoJpaRepository,
			JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.tipoIngressoJpaRepository = tipoIngressoJpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(Evento evento) {
		EventoJpa jpa = evento.getId() == null ? new EventoJpa()
				: jpaRepository.findById(evento.getId().getId()).orElseGet(EventoJpa::new);
		mapeador.preencherEventoJpa(jpa, evento);
		EventoJpa salvo = jpaRepository.save(jpa);
		if (evento.getId() == null) {
			evento.atribuirId(new EventoId(salvo.getId()));
		}
	}

	@Override
	public Evento obter(EventoId id) {
		return jpaRepository.findById(id.getId()).map(mapeador::paraEvento)
				.orElseThrow(() -> new IllegalArgumentException("evento não encontrado"));
	}

	@Override
	public void remover(EventoId id) {
		jpaRepository.deleteById(id.getId());
	}

	@Override
	public List<EventoResumo> pesquisarResumos() {
		return jpaRepository.pesquisarResumos();
	}

	@Override
	public List<EventoResumo> pesquisarAtivosFuturos() {
		return jpaRepository.pesquisarAtivosFuturos(StatusEvento.ATIVO, LocalDateTime.now());
	}

	@Override
	public Optional<EventoResumoExpandido> pesquisarResumoExpandido(EventoId id) {
		Optional<EventoResumo> base = jpaRepository.resumoPorId(id.getId());
		return base.map(e -> {
			List<TipoIngressoResumo> tipos = tipoIngressoJpaRepository.pesquisarResumosPorEvento(id.getId());
			return new EventoResumoExpandidoImpl(e, tipos);
		});
	}

	private record EventoResumoExpandidoImpl(EventoResumo base, List<TipoIngressoResumo> tipos)
			implements
				EventoResumoExpandido {

		@Override
		public List<TipoIngressoResumo> getTipos() {
			return tipos;
		}

		@Override
		public int getId() {
			return base.getId();
		}

		@Override
		public String getNome() {
			return base.getNome();
		}

		@Override
		public java.time.LocalDateTime getDataHora() {
			return base.getDataHora();
		}

		@Override
		public String getLocal() {
			return base.getLocal();
		}

		@Override
		public cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento getStatus() {
			return base.getStatus();
		}

		@Override
		public int getCapacidade() {
			return base.getCapacidade();
		}
	}
}
