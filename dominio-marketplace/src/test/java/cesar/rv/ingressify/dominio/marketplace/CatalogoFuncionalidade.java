package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CatalogoFuncionalidade extends MarketplaceFuncionalidade {

	private List<Evento> catalogo;

	@Given("3 eventos ativos cadastrados")
	public void tresEventosAtivosCadastrados() {
		for (int i = 1; i <= 3; i++) {
			LocalDateTime dh = LocalDateTime.now().plusDays(i * 10L);
			Evento e = MarketplaceTestData.eventoNovo("Evento " + i, dh, "Local " + i, "Descrição do evento " + i + ".",
					500);
			eventoServico.salvar(e);
			TipoIngresso tipo = new TipoIngresso(e.getId(), "Pista", new Dinheiro(new BigDecimal("100.00")), 200, 500,
					null);
			tipoIngressoServico.salvar(tipo);
		}
	}

	@Given("um evento ativo e um evento cancelado cadastrados")
	public void eventoAtivoECancelado() {
		LocalDateTime dhAtivo = LocalDateTime.now().plusDays(10);
		Evento ativo = MarketplaceTestData.eventoNovo("Evento Ativo", dhAtivo, "Parque Municipal",
				"Festival de música ao ar livre.", 500);
		eventoServico.salvar(ativo);
		TipoIngresso tipoAtivo = new TipoIngresso(ativo.getId(), "Pista", new Dinheiro(new BigDecimal("80.00")), 300,
				500, null);
		tipoIngressoServico.salvar(tipoAtivo);

		LocalDateTime dhCancelado = LocalDateTime.now().plusDays(20);
		Evento cancelado = MarketplaceTestData.eventoNovo("Evento Cancelado", dhCancelado, "Arena Norte",
				"Evento cancelado pela organização.", 500);
		eventoServico.salvar(cancelado);
		eventoServico.cancelar(cancelado.getId());
	}

	@Given("um evento ativo e um evento já iniciado cadastrados")
	public void eventoAtivoEIniciado() {
		LocalDateTime dhAtivo = LocalDateTime.now().plusDays(10);
		Evento ativo = MarketplaceTestData.eventoNovo("Evento Futuro", dhAtivo, "Teatro Municipal",
				"Espetáculo de dança contemporânea.", 300);
		eventoServico.salvar(ativo);
		TipoIngresso tipoAtivo = new TipoIngresso(ativo.getId(), "Plateia", new Dinheiro(new BigDecimal("60.00")), 200,
				300, null);
		tipoIngressoServico.salvar(tipoAtivo);

		LocalDateTime dhPassado = LocalDateTime.now().minusHours(2);
		Evento iniciado = MarketplaceTestData.eventoReidratado(new EventoId(99), "Evento Passado", dhPassado, "Ginásio",
				"Evento que já começou e não deve aparecer no catálogo.", StatusEvento.ATIVO, 200);
		eventoServico.salvar(iniciado);
	}

	@Given("nenhum evento cadastrado")
	public void nenhumEventoCadastrado() {
		// repositório começa vazio — nenhuma ação necessária
	}

	@When("visualizo o catálogo de eventos")
	public void visualizarCatalogo() {
		catalogo = eventoServico.listarAtivos();
	}

	@Then("o catálogo exibe 3 eventos")
	public void catalogoExibeTresEventos() {
		assertEquals(3, catalogo.size());
	}

	@Then("o catálogo exibe 1 evento")
	public void catalogoExibeUmEvento() {
		assertEquals(1, catalogo.size());
	}

	@Then("o catálogo está vazio")
	public void catalogoVazio() {
		assertTrue(catalogo.isEmpty());
	}
}
