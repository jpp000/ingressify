package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TransacaoFuncionalidade extends FinanceiroFuncionalidade {

	private static final UsuarioId USUARIO_1 = new UsuarioId(1);
	private List<Transacao> historico;

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
}
