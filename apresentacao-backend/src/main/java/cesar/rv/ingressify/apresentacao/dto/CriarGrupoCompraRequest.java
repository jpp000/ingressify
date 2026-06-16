package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CriarGrupoCompraRequest(
        Integer eventoId,
        Integer tipoIngressoId,
        LocalDateTime prazoPagamento,
        List<ParticipanteGrupoRequest> participantes
) {}
