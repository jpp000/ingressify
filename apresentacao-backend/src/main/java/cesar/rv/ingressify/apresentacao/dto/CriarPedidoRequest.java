package cesar.rv.ingressify.apresentacao.dto;

import java.util.List;

public record CriarPedidoRequest(
		List<ItemPedidoRequest> itens,
		String codigoCupom) {
}
