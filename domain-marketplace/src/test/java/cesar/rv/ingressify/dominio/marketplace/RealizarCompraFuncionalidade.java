package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertThrows;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RealizarCompraFuncionalidade {

	@When("inicio compra com quantidade zero")
	public void quantidadeZero() {
		// validação no Then
	}

	@Then("a operação é inválida")
	public void operacaoInvalida() {
		assertThrows(Exception.class, () -> Dinheiro.ZERO.multiplicar(-1));
	}
}
