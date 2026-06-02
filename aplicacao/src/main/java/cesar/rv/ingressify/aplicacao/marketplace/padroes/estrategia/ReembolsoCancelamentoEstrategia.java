package cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaReembolso;

public class ReembolsoCancelamentoEstrategia implements EstrategiaReembolso {

	@Override
	public void validar(LocalDateTime dataCompra, LocalDateTime dataEvento) {
		// aprovação automática: cancelamento de evento não tem restrição de prazo
	}
}
