package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

public record CriarSorteioRequest(
        Integer eventoId,
        Integer tipoIngressoId,
        Integer quantidadeIngressos,
        Integer quantidadeListaEspera,
        LocalDateTime prazoInscricao,
        Integer prazoConfirmacaoHoras
) {}
