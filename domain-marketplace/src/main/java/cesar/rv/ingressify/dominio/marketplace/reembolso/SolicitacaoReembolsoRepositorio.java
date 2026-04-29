package cesar.rv.ingressify.dominio.marketplace.reembolso;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface SolicitacaoReembolsoRepositorio {

	SolicitacaoReembolso obter(SolicitacaoReembolsoId id);

	void salvar(SolicitacaoReembolso solicitacao);

	Optional<SolicitacaoReembolso> pesquisarAtivaPorIngresso(IngressoId ingressoId);

	List<SolicitacaoReembolso> pesquisarPorSolicitante(UsuarioId solicitanteId);
}
