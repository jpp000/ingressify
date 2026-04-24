package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioResumo;
import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioResumoExpandido;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumo;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@Repository
public class AnuncioRevendaRepositorioImpl implements AnuncioRevendaRepositorio, AnuncioRepositorioAplicacao {

	private final AnuncioRevendaJpaRepository jpaRepository;
	private final IngressoJpaRepository ingressoJpaRepository;
	private final EventoJpaRepository eventoJpaRepository;
	private final JpaMapeador mapeador;

	public AnuncioRevendaRepositorioImpl(AnuncioRevendaJpaRepository jpaRepository, IngressoJpaRepository ingressoJpaRepository,
			EventoJpaRepository eventoJpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.ingressoJpaRepository = ingressoJpaRepository;
		this.eventoJpaRepository = eventoJpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(AnuncioRevenda anuncio) {
		AnuncioRevendaJpa jpa = anuncio.getId() == null ? new AnuncioRevendaJpa()
				: jpaRepository.findById(anuncio.getId().getId()).orElseGet(AnuncioRevendaJpa::new);
		mapeador.preencherAnuncioRevendaJpa(jpa, anuncio);
		AnuncioRevendaJpa salvo = jpaRepository.save(jpa);
		if (anuncio.getId() == null) {
			anuncio.atribuirId(new AnuncioRevendaId(salvo.getId()));
		}
	}

	@Override
	public AnuncioRevenda obter(AnuncioRevendaId id) {
		return jpaRepository.findById(id.getId()).map(mapeador::paraAnuncioRevenda)
				.orElseThrow(() -> new IllegalArgumentException("anúncio não encontrado"));
	}

	@Override
	public void remover(AnuncioRevendaId id) {
		jpaRepository.deleteById(id.getId());
	}

	@Override
	public Optional<AnuncioRevenda> obterPorCorrelacaoPagamento(UUID correlacao) {
		return jpaRepository.findByCorrelacaoPagamento(correlacao).map(mapeador::paraAnuncioRevenda);
	}

	@Override
	public boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId) {
		return jpaRepository.existsAtivoParaIngresso(ingressoId.getId());
	}

	@Override
	public List<AnuncioResumo> pesquisarResumos() {
		return jpaRepository.pesquisarResumos();
	}

	@Override
	public AnuncioResumoExpandido pesquisarResumoExpandido(AnuncioRevendaId id) {
		AnuncioRevendaJpa a = jpaRepository.findById(id.getId()).orElseThrow(() -> new IllegalArgumentException("anúncio não encontrado"));
		AnuncioResumo base = jpaRepository.obterResumo(a.getId());
		IngressoJpa ing = ingressoJpaRepository.findById(a.getIngressoId()).orElseThrow();
		IngressoResumo ingressoResumo = ingressoJpaRepository.obterResumo(ing.getId());
		EventoResumo evento = eventoJpaRepository.resumoPorId(ing.getEventoId()).orElseThrow();
		return new AnuncioResumoExpandidoImpl(base, ingressoResumo, evento);
	}

	private record AnuncioResumoExpandidoImpl(AnuncioResumo base, IngressoResumo ingresso, EventoResumo evento)
			implements
				AnuncioResumoExpandido {

		@Override
		public IngressoResumo getIngresso() {
			return ingresso;
		}

		@Override
		public EventoResumo getEvento() {
			return evento;
		}

		@Override
		public int getId() {
			return base.getId();
		}

		@Override
		public java.util.UUID getIngressoId() {
			return base.getIngressoId();
		}

		@Override
		public int getVendedorId() {
			return base.getVendedorId();
		}

		@Override
		public java.math.BigDecimal getPrecoValor() {
			return base.getPrecoValor();
		}

		@Override
		public Integer getCompradorReservadoId() {
			return base.getCompradorReservadoId();
		}

		@Override
		public cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio getStatus() {
			return base.getStatus();
		}
	}
}
