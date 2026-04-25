package cesar.rv.ingressify.dominio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class TransacaoFuncionalidade {

	private Transacao transacao;

	@Given("uma transação de compra de 100 reais")
	public void transacaoCompra() {
		transacao = new Transacao(new UsuarioId(1), TipoTransacao.COMPRA,
				new Dinheiro(new BigDecimal("100.00")), LocalDateTime.now(), UUID.randomUUID());
	}

	@Then("a transação possui tipo compra e valor 100 reais")
	public void verificarCompra() {
		assertEquals(TipoTransacao.COMPRA, transacao.getTipo());
		assertEquals(new Dinheiro(new BigDecimal("100.00")), transacao.getValor());
	}

	@Given("uma transação de venda de 90 reais")
	public void transacaoVenda() {
		transacao = new Transacao(new UsuarioId(1), TipoTransacao.VENDA,
				new Dinheiro(new BigDecimal("90.00")), LocalDateTime.now(), UUID.randomUUID());
	}

	@Then("a transação possui tipo venda e valor 90 reais")
	public void verificarVenda() {
		assertEquals(TipoTransacao.VENDA, transacao.getTipo());
		assertEquals(new Dinheiro(new BigDecimal("90.00")), transacao.getValor());
	}

	@Given("uma transação de ajuste de saldo de 200 reais")
	public void transacaoAjusteSaldo() {
		transacao = new Transacao(new UsuarioId(1), TipoTransacao.AJUSTE_SALDO,
				new Dinheiro(new BigDecimal("200.00")), LocalDateTime.now(), UUID.randomUUID());
	}

	@Then("a transação possui tipo ajuste de saldo e valor 200 reais")
	public void verificarAjuste() {
		assertEquals(TipoTransacao.AJUSTE_SALDO, transacao.getTipo());
		assertEquals(new Dinheiro(new BigDecimal("200.00")), transacao.getValor());
	}
}
