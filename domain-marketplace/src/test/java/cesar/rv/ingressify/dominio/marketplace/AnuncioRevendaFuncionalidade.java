package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AnuncioRevendaFuncionalidade {

	private AnuncioRevenda anuncio;
	private Throwable excecao;

	@Given("um anúncio de revenda disponível")
	public void anuncioDisponivel() {
		anuncio = new AnuncioRevenda(new IngressoId(UUID.randomUUID()), new UsuarioId(1),
				new Dinheiro(new BigDecimal("100.00")));
	}

	@When("reservo o anúncio para um comprador")
	public void reservarAnuncio() {
		anuncio.reservar(new UsuarioId(2), UUID.randomUUID());
	}

	@Then("o status do anúncio é reservado")
	public void statusAnuncioReservado() {
		assertEquals(StatusAnuncio.RESERVADO, anuncio.getStatus());
	}

	@Given("um anúncio de revenda reservado")
	public void anuncioReservado() {
		anuncio = new AnuncioRevenda(new AnuncioRevendaId(1), new IngressoId(UUID.randomUUID()), new UsuarioId(1),
				new Dinheiro(new BigDecimal("100.00")), new UsuarioId(2), StatusAnuncio.RESERVADO, UUID.randomUUID());
	}

	@When("concluo o anúncio de revenda")
	public void concluirAnuncio() {
		anuncio.concluir();
	}

	@Then("o status do anúncio é vendido")
	public void statusAnuncioVendido() {
		assertEquals(StatusAnuncio.VENDIDO, anuncio.getStatus());
	}

	@When("cancelo a reserva do anúncio")
	public void cancelarReservaAnuncio() {
		anuncio.cancelarReserva();
	}

	@Then("o status do anúncio volta a disponível")
	public void statusAnuncioVoltaDisponivel() {
		assertEquals(StatusAnuncio.DISPONIVEL, anuncio.getStatus());
	}

	@Given("um anúncio de revenda vendido")
	public void anuncioVendido() {
		anuncio = new AnuncioRevenda(new AnuncioRevendaId(1), new IngressoId(UUID.randomUUID()), new UsuarioId(1),
				new Dinheiro(new BigDecimal("100.00")), new UsuarioId(2), StatusAnuncio.VENDIDO, UUID.randomUUID());
	}

	@When("tento cancelar o anúncio vendido")
	public void tentarCancelarVendido() {
		try {
			anuncio.cancelar();
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("o cancelamento do anúncio é rejeitado")
	public void cancelamentoRejeitado() {
		assertNotNull(excecao);
	}

	@When("altero o preço do anúncio para 150 reais")
	public void alterarPreco() {
		anuncio.alterarPreco(new Dinheiro(new BigDecimal("150.00")));
	}

	@Then("o preço do anúncio é 150 reais")
	public void precoAtualizado() {
		assertEquals(new Dinheiro(new BigDecimal("150.00")), anuncio.getPreco());
	}

	@When("tento alterar o preço do anúncio reservado")
	public void tentarAlterarPrecoReservado() {
		try {
			anuncio.alterarPreco(new Dinheiro(new BigDecimal("200.00")));
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a alteração de preço é rejeitada")
	public void alteracaoPrecoRejeitada() {
		assertNotNull(excecao);
	}
}
