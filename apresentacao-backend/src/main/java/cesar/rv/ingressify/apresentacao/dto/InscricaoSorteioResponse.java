package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;

public record InscricaoSorteioResponse(
        int id,
        int sorteioId,
        int participanteId,
        LocalDateTime inscritoEm,
        StatusInscricao status,
        int posicao
) {
    public static InscricaoSorteioResponse fromDomain(InscricaoSorteio i) {
        return new InscricaoSorteioResponse(
                i.getId().getId(),
                i.getSorteioId().getId(),
                i.getParticipanteId().getId(),
                i.getInscritoEm(),
                i.getStatus(),
                i.getPosicao());
    }
}
