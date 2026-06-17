package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.ParticipanteGrupoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.ParticipanteGrupoSpringDataRepository;

@Repository
public class ParticipanteGrupoRepositorioPersistencia implements ParticipanteGrupoRepositorio {

    private final ParticipanteGrupoSpringDataRepository springData;

    public ParticipanteGrupoRepositorioPersistencia(ParticipanteGrupoSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public void salvar(ParticipanteGrupo participante) {
        ParticipanteGrupoJpa jpa = ParticipanteGrupoJpa.fromDomain(participante);
        ParticipanteGrupoJpa salvo = springData.save(jpa);
        if (participante.getId() == null) {
            participante.atribuirId(new ParticipanteGrupoId(salvo.getId()));
        }
    }

    @Override
    public List<ParticipanteGrupo> listarPorGrupo(GrupoCompraId grupoCompraId) {
        return springData.findByGrupoCompraId(grupoCompraId.getId()).stream()
                .map(ParticipanteGrupoJpa::toDomain)
                .toList();
    }

    @Override
    public Optional<ParticipanteGrupo> buscarPorGrupoEUsuario(GrupoCompraId grupoCompraId, UsuarioId usuarioId) {
        return springData.findByGrupoCompraIdAndUsuarioId(grupoCompraId.getId(), usuarioId.getId())
                .map(ParticipanteGrupoJpa::toDomain);
    }

    @Override
    public List<ParticipanteGrupo> listarPorUsuario(UsuarioId usuarioId) {
        return springData.findByUsuarioId(usuarioId.getId()).stream()
                .map(ParticipanteGrupoJpa::toDomain)
                .toList();
    }
}
