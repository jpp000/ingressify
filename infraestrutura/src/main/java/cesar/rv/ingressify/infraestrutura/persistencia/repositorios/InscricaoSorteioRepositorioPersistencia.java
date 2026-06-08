package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.InscricaoSorteioJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.InscricaoSorteioSpringDataRepository;

@Repository
public class InscricaoSorteioRepositorioPersistencia implements InscricaoSorteioRepositorio {

    private final InscricaoSorteioSpringDataRepository springData;

    public InscricaoSorteioRepositorioPersistencia(InscricaoSorteioSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public void salvar(InscricaoSorteio inscricao) {
        InscricaoSorteioJpa jpa = InscricaoSorteioJpa.fromDomain(inscricao);
        InscricaoSorteioJpa salvo = springData.save(jpa);
        if (inscricao.getId() == null) {
            inscricao.atribuirId(new InscricaoSorteioId(salvo.getId()));
        }
    }

    @Override
    public InscricaoSorteio obter(InscricaoSorteioId id) {
        return springData.findById(id.getId())
                .map(InscricaoSorteioJpa::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada: " + id));
    }

    @Override
    public List<InscricaoSorteio> pesquisarPorSorteio(SorteioId sorteioId) {
        return springData.findBySorteioId(sorteioId.getId()).stream()
                .map(InscricaoSorteioJpa::toDomain)
                .toList();
    }

    @Override
    public Optional<InscricaoSorteio> buscarPorSorteioEParticipante(SorteioId sorteioId, UsuarioId participanteId) {
        return springData.findBySorteioIdAndParticipanteId(sorteioId.getId(), participanteId.getId())
                .map(InscricaoSorteioJpa::toDomain);
    }

    @Override
    public boolean existeInscricaoAtiva(SorteioId sorteioId, UsuarioId participanteId) {
        return springData.findBySorteioIdAndParticipanteId(sorteioId.getId(), participanteId.getId())
                .map(i -> i.getStatus() != StatusInscricao.CANCELADO && i.getStatus() != StatusInscricao.EXPIRADO)
                .orElse(false);
    }
}
