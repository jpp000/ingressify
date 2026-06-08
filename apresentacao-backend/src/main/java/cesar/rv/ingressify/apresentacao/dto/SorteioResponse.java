package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public record SorteioResponse(
        int id,
        int eventoId,
        int tipoIngressoId,
        int organizadorId,
        int quantidadeIngressos,
        int quantidadeListaEspera,
        LocalDateTime prazoInscricao,
        int prazoConfirmacaoHoras,
        StatusSorteio status
) {
    public static SorteioResponse fromDomain(Sorteio s) {
        return new SorteioResponse(
                s.getId().getId(),
                s.getEventoId().getId(),
                s.getTipoIngressoId().getId(),
                s.getOrganizadorId().getId(),
                s.getQuantidadeIngressos(),
                s.getQuantidadeListaEspera(),
                s.getPrazoInscricao(),
                s.getPrazoConfirmacaoHoras(),
                s.getStatus());
    }
}
