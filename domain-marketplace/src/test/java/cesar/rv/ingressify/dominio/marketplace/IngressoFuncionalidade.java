package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class IngressoFuncionalidade {

	private Ingresso ingresso;
	private Throwable excecao;

	@Given("um ingresso ativo do usuário 1")
	public void umIngressoAtivoDoUsuario1() {
		ingresso = new Ingresso(new TipoIngressoId(1), new EventoId(1), new UsuarioId(1));
	}

	@When("transfiro o ingresso para o usuário 2")
	public void transferirParaUsuario2() {
		ingresso.transferir(new UsuarioId(2));
	}

	@Then("o ingresso pertence ao usuário 2")
	public void ingressoPertenceAoUsuario2() {
		assertEquals(new UsuarioId(2), ingresso.getProprietario());
	}

	@When("marco o ingresso para revenda")
	public void marcarParaRevenda() {
		ingresso.marcarEmRevenda();
	}

	@Then("o status do ingresso é em revenda")
	public void statusEmRevenda() {
		assertEquals(StatusIngresso.EM_REVENDA, ingresso.getStatus());
	}

	@Given("um ingresso em revenda")
	public void umIngressoEmRevenda() {
		ingresso = new Ingresso(new IngressoId(UUID.randomUUID()), new TipoIngressoId(1), new EventoId(1),
				new UsuarioId(1), StatusIngresso.EM_REVENDA);
	}

	@When("concluo a revenda do ingresso para o usuário 2")
	public void concluirRevendaParaUsuario2() {
		ingresso.concluirRevenda(new UsuarioId(2));
	}

	@Then("o status do ingresso é revendido")
	public void statusRevendido() {
		assertEquals(StatusIngresso.REVENDIDO, ingresso.getStatus());
	}

	@When("desmarca o ingresso da revenda")
	public void desmarcarRevenda() {
		ingresso.desmarcarRevenda();
	}

	@Then("o status do ingresso volta a ativo")
	public void statusVoltaAAtivo() {
		assertEquals(StatusIngresso.ATIVO, ingresso.getStatus());
	}

	@When("tento transferir o ingresso em revenda para o usuário 2")
	public void tentarTransferirEmRevenda() {
		try {
			ingresso.transferir(new UsuarioId(2));
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a operação no ingresso é rejeitada")
	public void operacaoNoIngressoRejeitada() {
		assertNotNull(excecao);
	}
}
