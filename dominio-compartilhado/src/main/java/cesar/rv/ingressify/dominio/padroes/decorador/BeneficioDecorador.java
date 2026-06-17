package cesar.rv.ingressify.dominio.padroes.decorador;

import org.apache.commons.lang3.Validate;

public class BeneficioDecorador extends IngressoDecorador {

	private final String beneficio;

	public BeneficioDecorador(Ingresso decorado, String beneficio) {
		super(decorado);
		Validate.notBlank(beneficio, "beneficio");
		this.beneficio = beneficio;
	}

	@Override
	public String obterDescricao() {
		return decorado.obterDescricao() + " [" + beneficio + "]";
	}
}
