package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public record ContextoCancelamento(
		EventoId eventoId,
		List<IngressoCancelado> ingressosCancelados) {
}
