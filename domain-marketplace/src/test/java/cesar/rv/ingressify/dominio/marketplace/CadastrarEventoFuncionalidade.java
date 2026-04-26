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

public class CadastrarEventoFuncionalidade extends MarketplaceFuncionalidade {

    private EventoId eventoId;

    @When("tento criar um evento com nome em branco")
    public void tentarNomeEmBranco() {
        try {
            new Evento("   ", LocalDateTime.now().plusDays(1), "Local X", "Festival de música.", 100);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @When("tento criar um evento com data no passado")
    public void tentarDataNoPassado() {
        try {
            new Evento("Show", LocalDateTime.now().minusDays(1), "Local X", "Show de rock e pop.", 100);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @When("tento criar um evento com capacidade zero")
    public void tentarCapacidadeZero() {
        try {
            new Evento("Show", LocalDateTime.now().plusDays(1), "Local X", "Festival de verão.", 0);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a criação é rejeitada")
    public void criacaoRejeitada() {
        assertNotNull(excecao);
    }

    @When("crio um evento com dados válidos")
    public void criarEventoValido() {
        Evento evento = new Evento("Festival de Verão", LocalDateTime.now().plusDays(30), "Estádio Municipal",
                "Dias de apresentação com atrações nacionais e internacionais. Open food e bebida.", 5000);
        eventoServico.salvar(evento);
        eventoId = evento.getId();
    }

    @Then("o evento é persistido com status ativo")
    public void eventoPeristidoComStatusAtivo() {
        assertNotNull(eventoId);
        assertEquals(StatusEvento.ATIVO, eventoServico.obter(eventoId).getStatus());
    }

    @Given("um evento ativo")
    public void umEventoAtivo() {
        Evento evento = new Evento("Show de Rock", LocalDateTime.now().plusDays(10), "Arena", "Bandas de rock e blues.", 1000);
        eventoServico.salvar(evento);
        eventoId = evento.getId();
    }

    @When("cancelo o evento")
    public void canceloOEvento() {
        eventoServico.cancelar(eventoId);
    }

    @Then("o status do evento é cancelado")
    public void statusEventoCancelado() {
        assertEquals(StatusEvento.CANCELADO, eventoServico.obter(eventoId).getStatus());
    }

    @When("atualizo o nome do evento")
    public void atualizarNomeEvento() {
        eventoServico.atualizar(eventoId, "Show de Rock - Edição Especial",
                LocalDateTime.now().plusDays(10), "Arena", "Edição com convidados especiais e abertura antecipada.", 1500);
    }

    @Then("o nome do evento é atualizado")
    public void nomeEventoAtualizado() {
        assertEquals("Show de Rock - Edição Especial", eventoServico.obter(eventoId).getNome());
    }

    @Given("um evento já iniciado")
    public void umEventoJaIniciado() {
        Evento evento = new Evento(new EventoId(99), "Show Passado", LocalDateTime.now().minusHours(2),
                "Teatro", "Espetáculo de teatro experimental em duas atuações.", StatusEvento.ATIVO, 200);
        eventoServico.salvar(evento);
        eventoId = evento.getId();
    }

    @When("tento atualizar os dados do evento")
    public void tentarAtualizarEvento() {
        try {
            eventoServico.atualizar(eventoId, "Novo Nome", LocalDateTime.now().plusDays(1), "Novo Local",
                    "Nova descrição após a mudança de data.", 300);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a atualização do evento é rejeitada")
    public void atualizacaoDoEventoRejeitada() {
        assertNotNull(excecao);
    }
}
