package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public record IngressoCancelado(
		IngressoId ingressoId,
		UsuarioId proprietario,
		Dinheiro valorReembolso) {
}
