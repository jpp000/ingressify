package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProcessarPagamentoFuncionalidade {

	private Pagamento pagamento;

	@Given("um pagamento pendente")
	public void pagamentoPendente() {
		pagamento = new Pagamento(new Dinheiro(java.math.BigDecimal.TEN), new UsuarioId(1), null, TipoOperacao.COMPRA_DIRETA,
				UUID.randomUUID());
	}

	@When("confirmo o pagamento")
	public void confirmo() {
		pagamento.confirmar();
	}

	@Then("o status passa a confirmado")
	public void statusConfirmado() {
		assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());
	}
}
