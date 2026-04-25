package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CadastrarEventoFuncionalidade {

	private Evento evento;
	private Throwable excecao;

	@When("tento criar um evento com nome em branco")
	public void tentarNomeEmBranco() {
		try {
			new Evento("   ", LocalDateTime.now().plusDays(1), "Local X", 100);
		} catch (Exception e) {
			excecao = e;
		}
	}

	@When("tento criar um evento com data no passado")
	public void tentarDataNoPassado() {
		try {
			new Evento("Show", LocalDateTime.now().minusDays(1), "Local X", 100);
		} catch (Exception e) {
			excecao = e;
		}
	}

	@When("tento criar um evento com capacidade zero")
	public void tentarCapacidadeZero() {
		try {
			new Evento("Show", LocalDateTime.now().plusDays(1), "Local X", 0);
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a criação é rejeitada")
	public void criacaoRejeitada() {
		assertNotNull(excecao);
	}

	@Given("um evento ativo")
	public void umEventoAtivo() {
		evento = new Evento("Show", LocalDateTime.now().plusDays(1), "Local X", 100);
	}

	@When("cancelo o evento")
	public void canceloOEvento() {
		evento.cancelar();
	}

	@Then("o status do evento é cancelado")
	public void statusEventoCancelado() {
		assertEquals(StatusEvento.CANCELADO, evento.getStatus());
	}

	@Given("um evento já iniciado")
	public void umEventoJaIniciado() {
		evento = new Evento(new EventoId(1), "Show", LocalDateTime.now().minusHours(1), "Local X", StatusEvento.ATIVO, 100);
	}

	@When("tento atualizar os dados do evento")
	public void tentarAtualizarEvento() {
		try {
			evento.atualizar("Show 2", LocalDateTime.now().plusDays(1), "Local Y", 200);
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a atualização do evento é rejeitada")
	public void atualizacaoDoEventoRejeitada() {
		assertNotNull(excecao);
	}
}
