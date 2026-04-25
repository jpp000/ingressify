package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProcessarPagamentoFuncionalidade {

	private Pagamento pagamento;
	private Throwable excecao;

	@Given("um pagamento pendente")
	public void pagamentoPendente() {
		pagamento = new Pagamento(new Dinheiro(java.math.BigDecimal.TEN), new UsuarioId(1), null,
				TipoOperacao.COMPRA_DIRETA, UUID.randomUUID());
	}

	@When("confirmo o pagamento")
	public void confirmo() {
		pagamento.confirmar();
	}

	@Then("o status passa a confirmado")
	public void statusConfirmado() {
		assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());
	}

	@When("rejeito o pagamento")
	public void rejeitoOPagamento() {
		pagamento.rejeitar();
	}

	@Then("o status passa a rejeitado")
	public void statusRejeitado() {
		assertEquals(StatusPagamento.REJEITADO, pagamento.getStatus());
	}

	@Given("um pagamento já confirmado")
	public void pagamentoJaConfirmado() {
		pagamento = new Pagamento(new PagamentoId(1), new Dinheiro(java.math.BigDecimal.TEN), new UsuarioId(1), null,
				TipoOperacao.COMPRA_DIRETA, StatusPagamento.CONFIRMADO, UUID.randomUUID());
	}

	@When("tento confirmar o pagamento novamente")
	public void tentarConfirmarNovamente() {
		try {
			pagamento.confirmar();
		} catch (Exception e) {
			excecao = e;
		}
	}

	@Then("a confirmação do pagamento é rejeitada")
	public void confirmacaoRejeitada() {
		assertNotNull(excecao);
	}
}
