package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaReembolso;
import cesar.rv.ingressify.dominio.padroes.estrategia.ReembolsoNaoPermitidoException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReembolsoFuncionalidade {

	private LocalDateTime dataCompra;
	private LocalDateTime dataEvento;
	private Throwable excecao;

	private final EstrategiaReembolso estrategiaVoluntaria = (dc, de) -> {
		LocalDateTime agora = LocalDateTime.now();
		if (dc.isBefore(agora.minusDays(7))) {
			throw new ReembolsoNaoPermitidoException("prazo de reembolso expirado: compra há mais de 7 dias");
		}
		if (!de.isAfter(agora.plusHours(48))) {
			throw new ReembolsoNaoPermitidoException("evento ocorre em menos de 48 horas");
		}
	};

	private final EstrategiaReembolso estrategiaCancelamento = (dc, de) -> {};

	@Given("a compra foi feita há {int} dias")
	public void compraMadeAgoDias(int dias) {
		dataCompra = LocalDateTime.now().minusDays(dias);
	}

	@And("o evento ocorre em {int} dias")
	public void eventoEmDias(int dias) {
		dataEvento = LocalDateTime.now().plusDays(dias);
	}

	@And("o evento ocorre em {int} horas")
	public void eventoEmHoras(int horas) {
		dataEvento = LocalDateTime.now().plusHours(horas);
	}

	@When("valido a estratégia de reembolso voluntário")
	public void validarEstrategiaVoluntaria() {
		try {
			estrategiaVoluntaria.validar(dataCompra, dataEvento);
		} catch (ReembolsoNaoPermitidoException e) {
			excecao = e;
		}
	}

	@When("valido a estratégia de reembolso por cancelamento")
	public void validarEstrategiaCancelamento() {
		try {
			estrategiaCancelamento.validar(dataCompra, dataEvento);
		} catch (ReembolsoNaoPermitidoException e) {
			excecao = e;
		}
	}

	@Then("a validação passa sem erro")
	public void validacaoPassaSemErro() {
		if (excecao != null) {
			throw new AssertionError("validação falhou inesperadamente: " + excecao.getMessage());
		}
	}

	@Then("a validação é rejeitada com {string}")
	public void validacaoRejeitadaCom(String fragmento) {
		assertNotNull(excecao, "exceção esperada mas não lançada");
		assertTrue(excecao.getMessage().contains(fragmento),
				"esperado '" + fragmento + "' em: " + excecao.getMessage());
	}
}
