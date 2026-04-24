package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendente;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendenteRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Repository
public class CompraPendenteRepositorioImpl implements CompraPendenteRepositorio {

	private final CompraPendenteJpaRepository jpaRepository;
	private final JpaMapeador mapeador;

	public CompraPendenteRepositorioImpl(CompraPendenteJpaRepository jpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(CompraPendente compra) {
		CompraPendenteJpa jpa = jpaRepository.findById(compra.getId()).orElseGet(CompraPendenteJpa::new);
		jpa.setId(compra.getId());
		jpa.setTipoIngressoId(compra.getTipoIngressoId().getId());
		jpa.setEventoId(compra.getEventoId().getId());
		jpa.setQuantidade(compra.getQuantidade());
		jpa.setCompradorId(compra.getComprador().getId());
		mapeador.aplicarDinheiro(jpa.getValorTotal(), compra.getValorTotal());
		jpa.setCriadaEm(compra.getCriadaEm());
		jpaRepository.save(jpa);
	}

	@Override
	public Optional<CompraPendente> buscar(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	public CompraPendente obter(UUID id) {
		return buscar(id).orElseThrow(() -> new IllegalArgumentException("compra pendente não encontrada"));
	}

	@Override
	public void remover(UUID id) {
		jpaRepository.deleteById(id);
	}

	private CompraPendente paraDominio(CompraPendenteJpa j) {
		return new CompraPendente(j.getId(), new TipoIngressoId(j.getTipoIngressoId()), new EventoId(j.getEventoId()), j.getQuantidade(),
				new UsuarioId(j.getCompradorId()), mapeador.paraDinheiro(j.getValorTotal()), j.getCriadaEm());
	}
}
