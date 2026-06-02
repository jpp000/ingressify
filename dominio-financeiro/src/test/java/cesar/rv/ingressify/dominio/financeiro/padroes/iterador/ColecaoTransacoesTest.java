package cesar.rv.ingressify.dominio.financeiro.padroes.iterador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

class ColecaoTransacoesTest {

	private static final UsuarioId USUARIO = new UsuarioId(1);

	private Transacao criarTransacao(int id, String valor, int diasAtras) {
		return new Transacao(
				new TransacaoId(id),
				USUARIO,
				TipoTransacao.COMPRA,
				new Dinheiro(new BigDecimal(valor)),
				LocalDateTime.now().minusDays(diasAtras),
				UUID.randomUUID());
	}

	@Test
	void iteradorPercorreTodosOsElementosDaPrimeiraPagina() {
		var colecao = new ColecaoTransacoes(List.of(
				criarTransacao(1, "100.00", 2),
				criarTransacao(2, "200.00", 1),
				criarTransacao(3, "300.00", 0)));

		var iter = colecao.criarIterador(1, 10);
		int contagem = 0;
		while (iter.temProximo()) {
			iter.proximo();
			contagem++;
		}
		assertEquals(3, contagem);
	}

	@Test
	void ordemCronologicaReversaRetornaMaisRecentePrimeiro() {
		var antiga = criarTransacao(1, "100.00", 5);
		var media = criarTransacao(2, "200.00", 2);
		var recente = criarTransacao(3, "300.00", 0);
		var colecao = new ColecaoTransacoes(List.of(antiga, media, recente));

		var iter = colecao.criarIterador(1, 10);
		assertEquals(recente.getId(), iter.proximo().getId());
		assertEquals(media.getId(), iter.proximo().getId());
		assertEquals(antiga.getId(), iter.proximo().getId());
	}

	@Test
	void paginacaoRetornaTotalCorretoParaCadaPagina() {
		var colecao = new ColecaoTransacoes(List.of(
				criarTransacao(1, "10.00", 4),
				criarTransacao(2, "20.00", 3),
				criarTransacao(3, "30.00", 2),
				criarTransacao(4, "40.00", 1),
				criarTransacao(5, "50.00", 0)));

		assertEquals(2, colecao.criarIterador(1, 2).totalElementos());
		assertEquals(2, colecao.criarIterador(2, 2).totalElementos());
		assertEquals(1, colecao.criarIterador(3, 2).totalElementos());
	}

	@Test
	void paginaAlemDoFimRetornaIteradorVazio() {
		var colecao = new ColecaoTransacoes(List.of(criarTransacao(1, "100.00", 0)));

		var iter = colecao.criarIterador(2, 10);
		assertFalse(iter.temProximo());
		assertEquals(0, iter.totalElementos());
	}

	@Test
	void temProximoRetornaTrueAntesDePrimeiraChamada() {
		var colecao = new ColecaoTransacoes(List.of(criarTransacao(1, "100.00", 0)));

		assertTrue(colecao.criarIterador(1, 10).temProximo());
	}

	@Test
	void temProximoRetornaFalseAposExaurirTodosOsElementos() {
		var colecao = new ColecaoTransacoes(List.of(criarTransacao(1, "100.00", 0)));

		var iter = colecao.criarIterador(1, 10);
		iter.proximo();
		assertFalse(iter.temProximo());
	}

	@Test
	void atualRetornaUltimaTransacaoConsumidaPorProximo() {
		var t = criarTransacao(1, "100.00", 0);
		var colecao = new ColecaoTransacoes(List.of(t));

		var iter = colecao.criarIterador(1, 10);
		var consumida = iter.proximo();
		assertSame(consumida, iter.atual());
	}

	@Test
	void atualLancaExcecaoSemNenhumConsumoPreio() {
		var colecao = new ColecaoTransacoes(List.of(criarTransacao(1, "100.00", 0)));

		assertThrows(IllegalStateException.class, () -> colecao.criarIterador(1, 10).atual());
	}

	@Test
	void proximoLancaExcecaoQuandoIteradorEstaExaurido() {
		var colecao = new ColecaoTransacoes(List.of());

		assertThrows(NoSuchElementException.class, () -> colecao.criarIterador(1, 10).proximo());
	}

	@Test
	void colecaoVaziaRetornaTotalZero() {
		assertEquals(0, new ColecaoTransacoes(List.of()).total());
	}

	@Test
	void limitePorPaginaIsolaCorretamenteCadaBloco() {
		var colecao = new ColecaoTransacoes(List.of(
				criarTransacao(1, "10.00", 2),
				criarTransacao(2, "20.00", 1),
				criarTransacao(3, "30.00", 0)));

		assertEquals(new TransacaoId(3), colecao.criarIterador(1, 1).proximo().getId());
		assertEquals(new TransacaoId(2), colecao.criarIterador(2, 1).proximo().getId());
		assertEquals(new TransacaoId(1), colecao.criarIterador(3, 1).proximo().getId());
	}
}
