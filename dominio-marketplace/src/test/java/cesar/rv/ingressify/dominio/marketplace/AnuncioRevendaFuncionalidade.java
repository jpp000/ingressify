package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AnuncioRevendaFuncionalidade extends MarketplaceFuncionalidade {

    private AnuncioRevendaId anuncioId;
    private List<IngressoId> correnteIds;

    private static final Dinheiro PRECO_PADRAO = new Dinheiro(new BigDecimal("100.00"));
    private static final UsuarioId VENDEDOR = new UsuarioId(1);
    private static final UsuarioId COMPRADOR = new UsuarioId(2);

    private void prepararVendaOficialEsgotadaEIngressoAtivo(int quantidadeIngressos) {
        LocalDateTime dh = LocalDateTime.now().plusDays(30);
        Evento e = MarketplaceTestData.eventoNovo("Festival de Revenda", dh, "Arena",
                "Festival de música eletrônica de um dia com vários Djs e artistas.", 10_000);
        eventoServico.salvar(e);
        TipoIngresso tipo = new TipoIngresso(e.getId(), "Pista", new Dinheiro(new BigDecimal("200.00")), 0, 5_000, null);
        tipoIngressoServico.salvar(tipo);
        correnteIds = new ArrayList<>();
        for (int n = 0; n < quantidadeIngressos; n++) {
            Ingresso i = new Ingresso(tipo.getId(), e.getId(), VENDEDOR);
            ingressoServico.salvar(i);
            correnteIds.add(i.getId());
        }
    }

    @Given("um anúncio de revenda disponível")
    public void anuncioDisponivel() {
        prepararVendaOficialEsgotadaEIngressoAtivo(1);
        AnuncioRevenda anuncio = anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
        anuncioId = anuncio.getId();
    }

    @When("reservo o anúncio para um comprador")
    public void reservarAnuncio() {
        anuncioRevendaServico.reservar(anuncioId, COMPRADOR, UUID.randomUUID());
    }

    @Then("o status do anúncio é reservado")
    public void statusAnuncioReservado() {
        assertEquals(StatusAnuncio.RESERVADO, anuncioRevendaServico.obter(anuncioId).getStatus());
    }

    @Given("um anúncio de revenda reservado")
    public void anuncioReservado() {
        prepararVendaOficialEsgotadaEIngressoAtivo(1);
        AnuncioRevenda anuncio = anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
        anuncioRevendaServico.reservar(anuncio.getId(), COMPRADOR, UUID.randomUUID());
        anuncioId = anuncio.getId();
    }

    @When("concluo o anúncio de revenda")
    public void concluirAnuncio() {
        anuncioRevendaServico.concluir(anuncioId);
    }

    @Then("o status do anúncio é vendido")
    public void statusAnuncioVendido() {
        assertEquals(StatusAnuncio.VENDIDO, anuncioRevendaServico.obter(anuncioId).getStatus());
    }

    @When("cancelo a reserva do anúncio")
    public void cancelarReservaAnuncio() {
        anuncioRevendaServico.cancelarReserva(anuncioId);
    }

    @Then("o status do anúncio volta a disponível")
    public void statusAnuncioVoltaDisponivel() {
        assertEquals(StatusAnuncio.DISPONIVEL, anuncioRevendaServico.obter(anuncioId).getStatus());
    }

    @Given("um anúncio de revenda vendido")
    public void anuncioVendido() {
        prepararVendaOficialEsgotadaEIngressoAtivo(1);
        AnuncioRevenda anuncio = anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
        anuncioRevendaServico.reservar(anuncio.getId(), COMPRADOR, UUID.randomUUID());
        anuncioRevendaServico.concluir(anuncio.getId());
        anuncioId = anuncio.getId();
    }

    @When("tento cancelar o anúncio vendido")
    public void tentarCancelarVendido() {
        try {
            anuncioRevendaServico.cancelar(anuncioId);
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
        anuncioRevendaServico.alterarPreco(anuncioId, new Dinheiro(new BigDecimal("150.00")));
    }

    @Then("o preço do anúncio é 150 reais")
    public void precoAtualizado() {
        assertEquals(new Dinheiro(new BigDecimal("150.00")), anuncioRevendaServico.obter(anuncioId).getPreco());
    }

    @When("tento alterar o preço do anúncio reservado")
    public void tentarAlterarPrecoReservado() {
        try {
            anuncioRevendaServico.alterarPreco(anuncioId, new Dinheiro(new BigDecimal("200.00")));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a alteração de preço é rejeitada")
    public void alteracaoPrecoRejeitada() {
        assertNotNull(excecao);
    }

    @Given("um ingresso pertencente ao usuário 1")
    public void ingressoDoUsuario1() {
        prepararVendaOficialEsgotadaEIngressoAtivo(1);
    }

    @When("tento criar um anúncio para esse ingresso como usuário 2")
    public void tentarCriarAnuncioComoUsuario2() {
        try {
            anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, new UsuarioId(2));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a criação do anúncio é rejeitada")
    public void criacaoAnuncioRejeitada() {
        assertNotNull(excecao);
    }

    @Given("um ingresso com anúncio já ativo")
    public void ingressoComAnuncioAtivo() {
        prepararVendaOficialEsgotadaEIngressoAtivo(1);
        anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
    }

    @When("tento criar mais um anúncio para o mesmo ingresso")
    public void tentarCriarSegundoAnuncio() {
        try {
            anuncioRevendaServico.criar(correnteIds, new Dinheiro(new BigDecimal("90.00")), VENDEDOR);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Then("a criação do segundo anúncio é rejeitada")
    public void criacaoSegundoAnuncioRejeitada() {
        assertNotNull(excecao);
    }

    @Given("venda oficial do evento ainda não esgotada")
    public void vendaOficialNaoEsgotada() {
        LocalDateTime dh = LocalDateTime.now().plusDays(20);
        Evento e = MarketplaceTestData.eventoNovo("Festival Soma", dh, "Parque", "Dia de música ao ar livre.", 5_000);
        eventoServico.salvar(e);
        TipoIngresso tipo = new TipoIngresso(e.getId(), "Pista", new Dinheiro(new BigDecimal("100.00")), 50, 1_000, null);
        tipoIngressoServico.salvar(tipo);
        Ingresso i = new Ingresso(tipo.getId(), e.getId(), VENDEDOR);
        ingressoServico.salvar(i);
        correnteIds = List.of(i.getId());
    }

    @When("tento criar anúncio de revenda nesse evento")
    public void tentarCriarAnuncioEsgotamentoBloqueado() {
        try {
            anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Given("vendedor com 2 ingressos e venda oficial do evento esgotada")
    public void vendedorComDoisEsgotado() {
        prepararVendaOficialEsgotadaEIngressoAtivo(2);
    }

    @When("crio um anúncio de revenda com esses ingressos no mesmo lote")
    public void crioAnuncioLote() {
        AnuncioRevenda a = anuncioRevendaServico.criar(correnteIds, PRECO_PADRAO, VENDEDOR);
        anuncioId = a.getId();
    }

    @Then("a quantidade anunciada é 2")
    public void quantidadeAnunciadaDois() {
        assertEquals(2, anuncioRevendaServico.obter(anuncioId).getQuantidade());
    }
}
