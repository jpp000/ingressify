package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TipoIngressoFuncionalidade {

	private TipoIngresso tipoIngresso;
	private Throwable excecao;

	@Given("um tipo de ingresso com 10 ingressos disponíveis")
	public void tipoComDezDisponiveis() {
		tipoIngresso = new TipoIngresso(new EventoId(1), "Pista", new Dinheiro(new BigDecimal("50.00")), 10, 10);
	}

	@When("reservo 3 ingressos do tipo")
	public void reservarTres() {
		tipoIngresso.reservar(3);
	}

	@Then("a quantidade disponível do tipo é 7")
	public void quantidadeDisponivelSete() {
		assertEquals(7, tipoIngresso.getQuantidadeDisponivel());
	}

	@Given("um tipo de ingresso sem ingressos disponíveis")
	public void tipoSemDisponiveis() {
		tipoIngresso = new TipoIngresso(new EventoId(1), "Pista", new Dinheiro(new BigDecimal("50.00")), 0, 10);
	}

	@When("tento reservar 1 ingresso do tipo indisponível")
	public void tentarReservarIndisponivel() {
		try {
			tipoIngresso.reservar(1);
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a reserva do ingresso é rejeitada")
	public void reservaRejeitada() {
		assertNotNull(excecao);
	}

	@Given("um tipo de ingresso com 5 ingressos disponíveis de um total de 10")
	public void tipoCincoDezTotal() {
		tipoIngresso = new TipoIngresso(new EventoId(1), "Pista", new Dinheiro(new BigDecimal("50.00")), 5, 10);
	}

	@When("devolvo 2 ingressos ao tipo")
	public void devolverDois() {
		tipoIngresso.devolver(2);
	}
}
