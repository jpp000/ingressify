package cesar.rv.ingressify.dominio.padroes.estrategia;

public class ReembolsoNaoPermitidoException extends RuntimeException {

	public ReembolsoNaoPermitidoException(String motivo) {
		super(motivo);
	}
}
