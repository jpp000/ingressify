package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TipoIngressoFuncionalidade extends MarketplaceFuncionalidade {

    private TipoIngressoId tipoIngressoId;
    private EventoId eventoId;

    private EventoId criarEventoComCapacidade(int capacidade) {
        Evento evento = new Evento("Show", LocalDateTime.now().plusDays(5), "Arena", "Apresentação ao vivo com repertório variado.", capacidade);
        eventoServico.salvar(evento);
        return evento.getId();
    }

    @Given("um tipo de ingresso com 10 ingressos disponíveis")
    public void tipoComDezDisponiveis() {
        eventoId = criarEventoComCapacidade(100);
        TipoIngresso tipo = new TipoIngresso(eventoId, "Pista", new Dinheiro(new BigDecimal("50.00")), 10, 10);
        tipoIngressoServico.salvar(tipo);
        tipoIngressoId = tipo.getId();
    }

    @When("reservo 3 ingressos do tipo")
    public void reservarTres() {
        TipoIngresso tipo = tipoIngressoServico.obter(tipoIngressoId);
        tipo.reservar(3);
        tipoIngressoServico.salvar(tipo);
    }

    @Then("a quantidade disponível do tipo é 7")
    public void quantidadeDisponivelSete() {
        assertEquals(7, tipoIngressoServico.obter(tipoIngressoId).getQuantidadeDisponivel());
    }

    @Given("um tipo de ingresso sem ingressos disponíveis")
    public void tipoSemDisponiveis() {
        eventoId = criarEventoComCapacidade(100);
        TipoIngresso tipo = new TipoIngresso(eventoId, "Pista", new Dinheiro(new BigDecimal("50.00")), 0, 10);
        tipoIngressoServico.salvar(tipo);
        tipoIngressoId = tipo.getId();
    }

    @When("tento reservar 1 ingresso do tipo indisponível")
    public void tentarReservarIndisponivel() {
        try {
            TipoIngresso tipo = tipoIngressoServico.obter(tipoIngressoId);
            tipo.reservar(1);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a reserva do ingresso é rejeitada")
    public void reservaRejeitada() {
        assertNotNull(excecao);
    }

    @Given("um tipo de ingresso com 5 ingressos disponíveis de um total de 10")
    public void tipoCincoDezTotal() {
        eventoId = criarEventoComCapacidade(100);
        TipoIngresso tipo = new TipoIngresso(eventoId, "Pista", new Dinheiro(new BigDecimal("50.00")), 5, 10);
        tipoIngressoServico.salvar(tipo);
        tipoIngressoId = tipo.getId();
    }

    @When("devolvo 2 ingressos ao tipo")
    public void devolverDois() {
        tipoIngressoServico.devolver(tipoIngressoId, 2);
    }

    @Given("um evento com capacidade de 10 lugares")
    public void eventoComCapacidadeDezLugares() {
        eventoId = criarEventoComCapacidade(10);
    }

    @When("cadastro um tipo de ingresso com 11 ingressos no total")
    public void cadastrarTipoComOnzeIngressos() {
        try {
            TipoIngresso tipo = new TipoIngresso(eventoId, "VIP", new Dinheiro(new BigDecimal("200.00")), 11, 11);
            tipoIngressoServico.salvar(tipo);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("o cadastro do tipo é rejeitado por exceder a capacidade")
    public void cadastroTipoRejeitadoPorCapacidade() {
        assertNotNull(excecao);
    }
}
