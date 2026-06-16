package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.GrupoCompraJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.GrupoCompraSpringDataRepository;

@Repository
public class GrupoCompraRepositorioPersistencia implements GrupoCompraRepositorio {

    private final GrupoCompraSpringDataRepository springData;

    public GrupoCompraRepositorioPersistencia(GrupoCompraSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public void salvar(GrupoCompra grupo) {
        GrupoCompraJpa jpa = GrupoCompraJpa.fromDomain(grupo);
        GrupoCompraJpa salvo = springData.save(jpa);
        if (grupo.getId() == null) {
            grupo.atribuirId(new GrupoCompraId(salvo.getId()));
        }
    }

    @Override
    public GrupoCompra obter(GrupoCompraId id) {
        return springData.findById(id.getId())
                .map(GrupoCompraJpa::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Grupo de compra não encontrado: " + id));
    }

    @Override
    public List<GrupoCompra> listarPorEvento(EventoId eventoId) {
        return springData.findByEventoId(eventoId.getId()).stream()
                .map(GrupoCompraJpa::toDomain)
                .toList();
    }

    @Override
    public List<GrupoCompra> listarPorLider(UsuarioId liderId) {
        return springData.findByLiderId(liderId.getId()).stream()
                .map(GrupoCompraJpa::toDomain)
                .toList();
    }

    @Override
    public List<GrupoCompra> listarAbertosExpirados(LocalDateTime agora) {
        return springData.findByStatusAndPrazoPagamentoBefore(StatusGrupoCompra.ABERTO, agora).stream()
                .map(GrupoCompraJpa::toDomain)
                .toList();
    }
}
