package cesar.rv.ingressify.apresentacao.dto;

public record ItemPedidoRequest(
		int tipoIngressoId,
		int quantidade,
		boolean meiaEntrada,
		String documento) {
}
