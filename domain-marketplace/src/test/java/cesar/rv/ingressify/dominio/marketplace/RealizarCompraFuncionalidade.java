package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RealizarCompraFuncionalidade extends MarketplaceFuncionalidade {

    private Pedido pedido;
    private TipoIngressoId tipoIngressoId;
    private EventoId eventoId;

    @When("inicio compra com quantidade zero")
    public void quantidadeZero() {
        try {
            new Pedido(UUID.randomUUID(), new TipoIngressoId(1), new EventoId(1), 0,
                    new UsuarioId(1), new Dinheiro(BigDecimal.TEN), LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @When("inicio compra com quantidade negativa")
    public void quantidadeNegativa() {
        try {
            new Pedido(UUID.randomUUID(), new TipoIngressoId(1), new EventoId(1), -1,
                    new UsuarioId(1), new Dinheiro(BigDecimal.TEN), LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a operação é inválida")
    public void operacaoInvalida() {
        assertNotNull(excecao);
    }

    @Given("um tipo de ingresso e evento válidos")
    public void tipoEEventoValidos() {
        tipoIngressoId = new TipoIngressoId(1);
        eventoId = new EventoId(1);
    }

    @When("inicio uma compra com quantidade válida")
    public void inicioCompraValida() {
        pedido = new Pedido(UUID.randomUUID(), tipoIngressoId, eventoId, 2,
                new UsuarioId(1), new Dinheiro(new BigDecimal("100.00")), LocalDateTime.now());
    }

    @Then("a compra pendente é criada com sucesso")
    public void compraPendenteCriadaComSucesso() {
        assertNotNull(pedido);
        assertEquals(2, pedido.getQuantidade());
        assertEquals(tipoIngressoId, pedido.getTipoIngressoId());
        assertEquals(eventoId, pedido.getEventoId());
    }
}
