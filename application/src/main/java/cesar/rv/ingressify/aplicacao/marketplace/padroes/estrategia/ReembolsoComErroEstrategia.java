package cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaReembolso;
import cesar.rv.ingressify.dominio.padroes.estrategia.ReembolsoNaoPermitidoException;

public class ReembolsoComErroEstrategia implements EstrategiaReembolso {

	@Override
	public void validar(LocalDateTime dataCompra, LocalDateTime dataEvento) {
		if (dataEvento.isBefore(LocalDateTime.now())) {
			throw new ReembolsoNaoPermitidoException("evento já ocorreu: reembolso por erro não aplicável");
		}
	}
}
