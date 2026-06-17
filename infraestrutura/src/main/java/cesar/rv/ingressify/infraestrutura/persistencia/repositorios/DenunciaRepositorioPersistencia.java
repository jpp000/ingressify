package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.Denuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.DenunciaJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.DenunciaSpringDataRepository;

@Repository
public class DenunciaRepositorioPersistencia implements DenunciaRepositorio {

	private final DenunciaSpringDataRepository jpa;

	public DenunciaRepositorioPersistencia(DenunciaSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Denuncia denuncia) {
		DenunciaJpa saved = jpa.save(DenunciaJpa.fromDomain(denuncia));
		if (denuncia.getId() == null) {
			denuncia.atribuirId(new DenunciaId(saved.getId()));
		}
	}

	@Override
	public Denuncia obter(DenunciaId id) {
		return jpa.findById(id.getId())
				.map(DenunciaJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Denuncia não encontrada: " + id.getId()));
	}

	@Override
	public boolean existePorAnuncioEDenunciante(AnuncioRevendaId anuncioId, UsuarioId denuncianteId) {
		return jpa.existsByAnuncioIdAndDenuncianteId(anuncioId.getId(), denuncianteId.getId());
	}

	@Override
	public List<Denuncia> pesquisarPendentes() {
		return jpa.findByStatus(StatusDenuncia.PENDENTE).stream()
				.map(DenunciaJpa::toDomain)
				.toList();
	}

	@Override
	public List<Denuncia> pesquisarTodas() {
		return jpa.findAllByOrderByCriadaEmDesc().stream()
				.map(DenunciaJpa::toDomain)
				.toList();
	}
}
