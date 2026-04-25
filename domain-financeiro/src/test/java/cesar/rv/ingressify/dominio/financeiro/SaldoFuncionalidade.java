package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SaldoFuncionalidade {

	private Saldo saldo;
	private Throwable excecao;

	@Given("um saldo de 100 reais")
	public void saldoCem() {
		saldo = new Saldo(new UsuarioId(1), new Dinheiro(new BigDecimal("100.00")));
	}

	@When("credito 50 reais ao saldo")
	public void creditarCinquenta() {
		saldo.creditar(new Dinheiro(new BigDecimal("50.00")));
	}

	@Then("o saldo passa a 150 reais")
	public void saldoCentoECinquenta() {
		assertEquals(new Dinheiro(new BigDecimal("150.00")), saldo.getValor());
	}

	@When("debito 30 reais do saldo")
	public void debitarTrinta() {
		saldo.debitar(new Dinheiro(new BigDecimal("30.00")));
	}

	@Then("o saldo passa a 70 reais")
	public void saldoSetenta() {
		assertEquals(new Dinheiro(new BigDecimal("70.00")), saldo.getValor());
	}

	@Given("um saldo de 20 reais")
	public void saldoVinte() {
		saldo = new Saldo(new UsuarioId(1), new Dinheiro(new BigDecimal("20.00")));
	}

	@When("tento debitar 50 reais do saldo")
	public void tentarDebitarCinquenta() {
		try {
			saldo.debitar(new Dinheiro(new BigDecimal("50.00")));
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("o débito é rejeitado")
	public void debitoRejeitado() {
		assertNotNull(excecao);
	}
}
