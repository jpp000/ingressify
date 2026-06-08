package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface InscricaoSorteioRepositorio {

    void salvar(InscricaoSorteio inscricao);

    InscricaoSorteio obter(InscricaoSorteioId id);

    List<InscricaoSorteio> pesquisarPorSorteio(SorteioId sorteioId);

    Optional<InscricaoSorteio> buscarPorSorteioEParticipante(SorteioId sorteioId, UsuarioId participanteId);

    boolean existeInscricaoAtiva(SorteioId sorteioId, UsuarioId participanteId);
}
