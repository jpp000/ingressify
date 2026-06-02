package cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaReembolso;
import cesar.rv.ingressify.dominio.padroes.estrategia.ReembolsoNaoPermitidoException;

public class ReembolsoVoluntarioEstrategia implements EstrategiaReembolso {

	@Override
	public void validar(LocalDateTime dataCompra, LocalDateTime dataEvento) {
		LocalDateTime agora = LocalDateTime.now();
		if (dataCompra.isBefore(agora.minusDays(7))) {
			throw new ReembolsoNaoPermitidoException("prazo de reembolso expirado: compra há mais de 7 dias");
		}
		if (!dataEvento.isAfter(agora.plusHours(48))) {
			throw new ReembolsoNaoPermitidoException("evento ocorre em menos de 48 horas");
		}
	}
}
