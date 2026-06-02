package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.util.List;

public record CriarAnuncioRequest(List<String> ingressoIds, BigDecimal preco) {}
