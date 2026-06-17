package cesar.rv.ingressify.dominio.padroes.estrategia;

import java.time.LocalDateTime;

public interface EstrategiaReembolso {

	void validar(LocalDateTime dataCompra, LocalDateTime dataEvento);
}
