package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProcessarPagamentoFuncionalidade extends FinanceiroFuncionalidade {

    private PagamentoId pagamentoId;

    @Given("um pagamento pendente")
    public void pagamentoPendente() {
        var pagamento = pagamentoServico.criarPendente(
                new Dinheiro(java.math.BigDecimal.TEN),
                new UsuarioId(1),
                null,
                TipoOperacao.COMPRA_DIRETA,
                UUID.randomUUID());
        pagamentoId = pagamento.getId();
    }

    @When("confirmo o pagamento")
    public void confirmo() {
        pagamentoServico.confirmar(pagamentoId);
    }

    @Then("o status passa a confirmado")
    public void statusConfirmado() {
        assertEquals(StatusPagamento.CONFIRMADO, pagamentoServico.obter(pagamentoId).getStatus());
    }

    @When("rejeito o pagamento")
    public void rejeitoOPagamento() {
        pagamentoServico.rejeitar(pagamentoId);
    }

    @Then("o status passa a rejeitado")
    public void statusRejeitado() {
        assertEquals(StatusPagamento.REJEITADO, pagamentoServico.obter(pagamentoId).getStatus());
    }

    @And("tento confirmar o pagamento rejeitado")
    public void tentarConfirmarPagamentoRejeitado() {
        try {
            pagamentoServico.confirmar(pagamentoId);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Given("um pagamento já confirmado")
    public void pagamentoJaConfirmado() {
        var pagamento = pagamentoServico.criarPendente(
                new Dinheiro(java.math.BigDecimal.TEN),
                new UsuarioId(1),
                null,
                TipoOperacao.COMPRA_DIRETA,
                UUID.randomUUID());
        pagamentoId = pagamento.getId();
        pagamentoServico.confirmar(pagamentoId);
    }

    @When("tento confirmar o pagamento novamente")
    public void tentarConfirmarNovamente() {
        try {
            pagamentoServico.confirmar(pagamentoId);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a confirmação do pagamento é rejeitada")
    public void confirmacaoRejeitada() {
        assertNotNull(excecao);
    }
}
