package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

public record CompraAssentoResponse(BigDecimal totalDebitado, String mensagem) {
}
