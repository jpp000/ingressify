package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AnuncioRevendaJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.AnuncioRevendaSpringDataRepository;

@Repository
public class AnuncioRevendaRepositorioPersistencia implements AnuncioRevendaRepositorio {

	private static final List<StatusAnuncio> ATIVOS = List.of(StatusAnuncio.DISPONIVEL, StatusAnuncio.RESERVADO);

	private final AnuncioRevendaSpringDataRepository jpa;

	public AnuncioRevendaRepositorioPersistencia(AnuncioRevendaSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(AnuncioRevenda anuncio) {
		AnuncioRevendaJpa saved = jpa.save(AnuncioRevendaJpa.fromDomain(anuncio));
		if (anuncio.getId() == null) {
			anuncio.atribuirId(new AnuncioRevendaId(saved.getId()));
		}
	}

	@Override
	public AnuncioRevenda obter(AnuncioRevendaId id) {
		return jpa.findById(id.getId())
				.map(AnuncioRevendaJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("AnuncioRevenda não encontrado: " + id.getId()));
	}

	@Override
	public void remover(AnuncioRevendaId id) {
		jpa.deleteById(id.getId());
	}

	@Override
	public boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId) {
		return jpa.existeAtivoPorIngresso(ingressoId.getId(), ATIVOS);
	}

	@Override
	public boolean existeDisponivelOuReservadoParaEvento(EventoId eventoId) {
		return jpa.existeAtivoPorEvento(eventoId.getId(), ATIVOS);
	}

	@Override
	public List<AnuncioRevenda> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(AnuncioRevendaJpa::toDomain)
				.toList();
	}

	@Override
	public List<AnuncioRevenda> pesquisarPorVendedor(UsuarioId vendedorId) {
		return jpa.findByVendedorId(vendedorId.getId()).stream()
				.map(AnuncioRevendaJpa::toDomain)
				.toList();
	}

	@Override
	public List<AnuncioRevenda> listarTodos() {
		return jpa.findAll().stream()
				.map(AnuncioRevendaJpa::toDomain)
				.toList();
	}
}
