package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.ColecaoTransacoes;
import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.TransacaoIterador;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TransacaoFuncionalidade extends FinanceiroFuncionalidade {

	private static final UsuarioId USUARIO_1 = new UsuarioId(1);
	private List<Transacao> historico;
	private TransacaoIterador iterador;

	@Given("uma transação de compra de 100 reais registrada para o usuário 1")
	public void transacaoCompraRegistrada() {
		Transacao t = new Transacao(USUARIO_1, TipoTransacao.COMPRA,
				new Dinheiro(new BigDecimal("100.00")), LocalDateTime.now(), UUID.randomUUID());
		transacaoRepositorio.salvar(t);
	}

	@Given("uma transação de venda de 90 reais registrada para o usuário 1")
	public void transacaoVendaRegistrada() {
		Transacao t = new Transacao(USUARIO_1, TipoTransacao.VENDA,
				new Dinheiro(new BigDecimal("90.00")), LocalDateTime.now(), UUID.randomUUID());
		transacaoRepositorio.salvar(t);
	}

	@When("busco o histórico do usuário 1")
	public void buscarHistorico() {
		historico = transacaoRepositorio.pesquisarPorUsuario(USUARIO_1);
	}

	@Then("o histórico contém 1 transação do tipo compra com valor 100 reais")
	public void historicoComUmaCompra() {
		assertEquals(1, historico.size());
		assertEquals(TipoTransacao.COMPRA, historico.get(0).getTipo());
		assertEquals(new Dinheiro(new BigDecimal("100.00")), historico.get(0).getValor());
	}

	@Then("o histórico contém 1 transação do tipo venda com valor 90 reais")
	public void historicoComUmaVenda() {
		assertEquals(1, historico.size());
		assertEquals(TipoTransacao.VENDA, historico.get(0).getTipo());
		assertEquals(new Dinheiro(new BigDecimal("90.00")), historico.get(0).getValor());
	}

	@Then("o histórico contém 2 transações")
	public void historicoComDuasTransacoes() {
		assertEquals(2, historico.size());
	}

	@When("itero o histórico do usuário 1 com o iterador")
	public void iterarHistoricoComIterador() {
		List<Transacao> lista = transacaoRepositorio.pesquisarPorUsuario(USUARIO_1);
		ColecaoTransacoes colecao = new ColecaoTransacoes(lista);
		iterador = colecao.criarIterador(1, 10);
	}

	@Then("o iterador percorre {int} transações no total")
	public void iteradorPercorreTotal(int esperado) {
		int contagem = 0;
		while (iterador.temProximo()) {
			iterador.proximo();
			contagem++;
		}
		assertEquals(esperado, contagem);
	}

	@Then("o iterador tem próximo elemento antes de iterar")
	public void iteradorTemProximo() {
		assertTrue(iterador.temProximo());
	}

	@And("após consumir todos os elementos o iterador não tem próximo")
	public void iteradorSemProximoAposConsumo() {
		while (iterador.temProximo()) {
			iterador.proximo();
		}
		assertFalse(iterador.temProximo());
	}

	@Then("o iterador não tem próximo elemento")
	public void iteradorSemProximo() {
		assertFalse(iterador.temProximo());
	}
}
