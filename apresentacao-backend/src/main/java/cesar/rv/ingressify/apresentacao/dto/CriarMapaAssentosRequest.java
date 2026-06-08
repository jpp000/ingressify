package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

public record CriarMapaAssentosRequest(
        Integer eventoId,
        Integer totalLinhas,
        Integer totalColunas,
        BigDecimal precoNormal,
        BigDecimal precoVip
) {}
