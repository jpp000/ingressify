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

public class SaldoFuncionalidade extends FinanceiroFuncionalidade {

	private static final UsuarioId USUARIO = new UsuarioId(1);

	@Given("um saldo de 100 reais")
	public void saldoCem() {
		saldoRepositorio.salvar(new Saldo(USUARIO, new Dinheiro(new BigDecimal("100.00"))));
	}

	@When("credito 50 reais ao saldo")
	public void creditarCinquenta() {
		saldoServico.creditar(USUARIO, new Dinheiro(new BigDecimal("50.00")));
	}

	@Then("o saldo passa a 150 reais")
	public void saldoCentoECinquenta() {
		assertEquals(new Dinheiro(new BigDecimal("150.00")), saldoServico.obter(USUARIO).getValor());
	}

	@When("debito 30 reais do saldo")
	public void debitarTrinta() {
		saldoServico.debitar(USUARIO, new Dinheiro(new BigDecimal("30.00")));
	}

	@Then("o saldo passa a 70 reais")
	public void saldoSetenta() {
		assertEquals(new Dinheiro(new BigDecimal("70.00")), saldoServico.obter(USUARIO).getValor());
	}

	@Given("um saldo de 20 reais")
	public void saldoVinte() {
		saldoRepositorio.salvar(new Saldo(USUARIO, new Dinheiro(new BigDecimal("20.00"))));
	}

	@When("tento debitar 50 reais do saldo")
	public void tentarDebitarCinquenta() {
		try {
			saldoServico.debitar(USUARIO, new Dinheiro(new BigDecimal("50.00")));
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("o débito é rejeitado")
	public void debitoRejeitado() {
		assertNotNull(excecao);
	}
}
