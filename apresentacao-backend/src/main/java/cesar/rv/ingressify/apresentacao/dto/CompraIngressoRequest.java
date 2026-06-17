package cesar.rv.ingressify.apresentacao.dto;

public record CompraIngressoRequest(
		int tipoIngressoId,
		int quantidade,
		boolean meiaEntrada,
		String documento,
		String codigoCupom) {
}
