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

public class IngressoFuncionalidade extends MarketplaceFuncionalidade {

    private IngressoId ingressoId;

    @Given("um ingresso ativo do usuário 1")
    public void umIngressoAtivoDoUsuario1() {
        Ingresso ingresso = new Ingresso(new TipoIngressoId(1), new EventoId(1), new UsuarioId(1));
        ingressoServico.salvar(ingresso);
        ingressoId = ingresso.getId();
    }

    @When("transfiro o ingresso para o usuário 2")
    public void transferirParaUsuario2() {
        ingressoServico.transferir(ingressoId, new UsuarioId(2));
    }

    @Then("o ingresso pertence ao usuário 2")
    public void ingressoPertenceAoUsuario2() {
        assertEquals(new UsuarioId(2), ingressoServico.obter(ingressoId).getProprietario());
    }

    @When("marco o ingresso para revenda")
    public void marcarParaRevenda() {
        Ingresso ingresso = ingressoServico.obter(ingressoId);
        ingresso.marcarEmRevenda();
        ingressoServico.salvar(ingresso);
    }

    @Then("o status do ingresso é em revenda")
    public void statusEmRevenda() {
        assertEquals(StatusIngresso.EM_REVENDA, ingressoServico.obter(ingressoId).getStatus());
    }

    @Given("um ingresso em revenda")
    public void umIngressoEmRevenda() {
        IngressoId id = new IngressoId(UUID.randomUUID());
        Ingresso ingresso = new Ingresso(id, new TipoIngressoId(1), new EventoId(1),
                new UsuarioId(1), StatusIngresso.EM_REVENDA);
        ingressoServico.salvar(ingresso);
        ingressoId = id;
    }

    @When("concluo a revenda do ingresso para o usuário 2")
    public void concluirRevendaParaUsuario2() {
        ingressoServico.concluirRevenda(ingressoId, new UsuarioId(2));
    }

    @Then("o status do ingresso é revendido")
    public void statusRevendido() {
        assertEquals(StatusIngresso.REVENDIDO, ingressoServico.obter(ingressoId).getStatus());
    }

    @When("desmarca o ingresso da revenda")
    public void desmarcarRevenda() {
        Ingresso ingresso = ingressoServico.obter(ingressoId);
        ingresso.desmarcarRevenda();
        ingressoServico.salvar(ingresso);
    }

    @Then("o status do ingresso volta a ativo")
    public void statusVoltaAAtivo() {
        assertEquals(StatusIngresso.ATIVO, ingressoServico.obter(ingressoId).getStatus());
    }

    @When("tento transferir o ingresso em revenda para o usuário 2")
    public void tentarTransferirEmRevenda() {
        try {
            ingressoServico.transferir(ingressoId, new UsuarioId(2));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a operação no ingresso é rejeitada")
    public void operacaoNoIngressoRejeitada() {
        assertNotNull(excecao);
    }
}
