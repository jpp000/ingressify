package cesar.rv.ingressify.dominio.padroes.decorador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

class DescontoDecoradorTest {

	private static final Dinheiro DUZENTOS = new Dinheiro(new BigDecimal("200.00"));

	@Test
	void ingressoBaseRetornaPrecoOriginal() {
		Ingresso ingresso = new IngressoBase(200);
		assertEquals(DUZENTOS, ingresso.obterPreco());
	}

	@Test
	void descontoDezPorCentoSobreDuzentosRetornaCentoeOitenta() {
		Ingresso ingresso = new IngressoBase(200);
		Ingresso comDesconto = new DescontoDecorador(ingresso, 10);
		assertEquals(new Dinheiro(new BigDecimal("180.00")), comDesconto.obterPreco());
	}

	@Test
	void descontoVintePorCentoSobreDuzentosRetornaCentoesSessenta() {
		Ingresso ingresso = new IngressoBase(200);
		Ingresso comDesconto = new DescontoDecorador(ingresso, 20);
		assertEquals(new Dinheiro(new BigDecimal("160.00")), comDesconto.obterPreco());
	}

	@Test
	void descontoCinquentaPorCentoReducMedadeDoPreco() {
		Ingresso ingresso = new IngressoBase(200);
		Ingresso comDesconto = new DescontoDecorador(ingresso, 50);
		assertEquals(new Dinheiro(new BigDecimal("100.00")), comDesconto.obterPreco());
	}

	@Test
	void encadeamentoDoisDescontosAplicaSequencialmente() {
		Ingresso ingresso = new IngressoBase(200);
		Ingresso primeirDesconto = new DescontoDecorador(ingresso, 10);   // 200 → 180
		Ingresso segundoDesconto = new DescontoDecorador(primeirDesconto, 10); // 180 → 162
		assertEquals(new Dinheiro(new BigDecimal("162.00")), segundoDesconto.obterPreco());
	}

	@Test
	void beneficioNaoAlteraPreco() {
		Ingresso ingresso = new IngressoBase(200);
		Ingresso comBeneficio = new BeneficioDecorador(ingresso, "Acesso VIP");
		assertEquals(DUZENTOS, comBeneficio.obterPreco());
	}

	@Test
	void beneficioAdicionaTextoNaDescricao() {
		Ingresso ingresso = new IngressoBase(new BigDecimal("200.00"), "Pista");
		Ingresso comBeneficio = new BeneficioDecorador(ingresso, "Open Bar");
		assertTrue(comBeneficio.obterDescricao().contains("Open Bar"));
	}

	@Test
	void encadeamentoDescontoMaisBeneficio() {
		Ingresso ingresso = new IngressoBase(new BigDecimal("200.00"), "Pista");
		Ingresso comBeneficio = new BeneficioDecorador(ingresso, "VIP");
		Ingresso comDesconto = new DescontoDecorador(comBeneficio, 10);
		assertEquals(new Dinheiro(new BigDecimal("180.00")), comDesconto.obterPreco());
		assertTrue(comDesconto.obterDescricao().contains("VIP"));
		assertTrue(comDesconto.obterDescricao().contains("-10%"));
	}

	@Test
	void descontoZeroLancaExcecao() {
		Ingresso ingresso = new IngressoBase(200);
		assertThrows(IllegalArgumentException.class, () -> new DescontoDecorador(ingresso, 0));
	}

	@Test
	void desconto100LancaExcecao() {
		Ingresso ingresso = new IngressoBase(200);
		assertThrows(IllegalArgumentException.class, () -> new DescontoDecorador(ingresso, 100));
	}
}
