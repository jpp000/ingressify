package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CadastrarEventoFuncionalidade {

	@When("tento criar um evento com nome em branco")
	public void tentarNomeEmBranco() {
		// executado no Then para capturar exceção
	}

	@Then("a criação é rejeitada")
	public void criacaoRejeitada() {
		assertThrows(Exception.class,
				() -> new Evento("   ", LocalDateTime.now().plusDays(1), "Local X", 100));
	}
}
