package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.IngressoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.IngressoSpringDataRepository;

@Repository
public class IngressoRepositorioPersistencia implements IngressoRepositorio {

	private final IngressoSpringDataRepository jpa;

	public IngressoRepositorioPersistencia(IngressoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Ingresso ingresso) {
		IngressoJpa saved = jpa.save(IngressoJpa.fromDomain(ingresso));
		if (ingresso.getId() == null) {
			ingresso.atribuirId(new IngressoId(saved.getId()));
		}
	}

	@Override
	public Ingresso obter(IngressoId id) {
		return jpa.findById(id.getId())
				.map(IngressoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Ingresso nao encontrado: " + id));
	}

	@Override
	public Ingresso obterPorCodigo(String codigo) {
		return jpa.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
				.map(IngressoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Ingresso nao encontrado para o codigo informado"));
	}

	@Override
	public List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario) {
		return jpa.findByProprietarioId(proprietario.getId()).stream()
				.map(IngressoJpa::toDomain).toList();
	}

	@Override
	public List<Ingresso> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(IngressoJpa::toDomain).toList();
	}

	@Override
	public List<Ingresso> pesquisarPorTipo(TipoIngressoId tipoIngressoId) {
		return jpa.findByTipoIngressoId(tipoIngressoId.getId()).stream()
				.map(IngressoJpa::toDomain).toList();
	}

	@Override
	public int contarVendidosPorTipo(TipoIngressoId tipoIngressoId) {
		return jpa.contarVendidosPorTipo(tipoIngressoId.getId(),
				StatusIngresso.CANCELADO, StatusIngresso.REEMBOLSADO);
	}
}
