package cesar.rv.ingressify.dominio.padroes.estrategia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class EstrategiaReembolsoTest {

	private static final LocalDateTime EVENTO_FUTURO_3_DIAS = LocalDateTime.now().plusDays(3);
	private static final LocalDateTime EVENTO_DAQUI_1_HORA = LocalDateTime.now().plusHours(1);
	private static final LocalDateTime COMPRA_HOJE = LocalDateTime.now().minusDays(1);
	private static final LocalDateTime COMPRA_HA_8_DIAS = LocalDateTime.now().minusDays(8);

	// ── Voluntário ────────────────────────────────────────────────────────────

	@Test
	void voluntario_dentroDoPrazo_permitido() {
		var estrategia = estrategiaVoluntaria();
		assertDoesNotThrow(() -> estrategia.validar(COMPRA_HOJE, EVENTO_FUTURO_3_DIAS));
	}

	@Test
	void voluntario_compraMaisde7Dias_lancaExcecao() {
		var estrategia = estrategiaVoluntaria();
		var ex = assertThrows(ReembolsoNaoPermitidoException.class,
				() -> estrategia.validar(COMPRA_HA_8_DIAS, EVENTO_FUTURO_3_DIAS));
		assertTrue(ex.getMessage().contains("7 dias"));
	}

	@Test
	void voluntario_eventoMenosDe48h_lancaExcecao() {
		var estrategia = estrategiaVoluntaria();
		assertThrows(ReembolsoNaoPermitidoException.class,
				() -> estrategia.validar(COMPRA_HOJE, EVENTO_DAQUI_1_HORA));
	}

	@Test
	void voluntario_naLimiteExato7dias_permitido() {
		var compraHa6Dias = LocalDateTime.now().minusDays(6).plusHours(1);
		var estrategia = estrategiaVoluntaria();
		assertDoesNotThrow(() -> estrategia.validar(compraHa6Dias, EVENTO_FUTURO_3_DIAS));
	}

	// ── Cancelamento ──────────────────────────────────────────────────────────

	@Test
	void cancelamento_semprePermitido_independenteDaDatas() {
		var estrategia = estrategiaCancelamento();
		assertDoesNotThrow(() -> estrategia.validar(COMPRA_HA_8_DIAS, EVENTO_DAQUI_1_HORA));
		assertDoesNotThrow(() -> estrategia.validar(COMPRA_HA_8_DIAS, EVENTO_FUTURO_3_DIAS));
	}

	@Test
	void cancelamento_compraAntiga_aindaPermitido() {
		var estrategia = estrategiaCancelamento();
		assertDoesNotThrow(() -> estrategia.validar(LocalDateTime.now().minusDays(365), EVENTO_DAQUI_1_HORA));
	}

	// ── Erro ──────────────────────────────────────────────────────────────────

	@Test
	void erro_eventoFuturo_permitido() {
		var estrategia = estrategiaErro();
		assertDoesNotThrow(() -> estrategia.validar(COMPRA_HOJE, EVENTO_FUTURO_3_DIAS));
	}

	@Test
	void erro_eventoPassado_lancaExcecao() {
		var estrategia = estrategiaErro();
		var eventoOntem = LocalDateTime.now().minusDays(1);
		assertThrows(ReembolsoNaoPermitidoException.class,
				() -> estrategia.validar(COMPRA_HOJE, eventoOntem));
	}

	// ── Factories ─────────────────────────────────────────────────────────────

	private EstrategiaReembolso estrategiaVoluntaria() {
		return (dataCompra, dataEvento) -> {
			var agora = LocalDateTime.now();
			if (dataCompra.isBefore(agora.minusDays(7))) {
				throw new ReembolsoNaoPermitidoException("prazo de reembolso expirado: compra há mais de 7 dias");
			}
			if (!dataEvento.isAfter(agora.plusHours(48))) {
				throw new ReembolsoNaoPermitidoException("evento ocorre em menos de 48 horas");
			}
		};
	}

	private EstrategiaReembolso estrategiaCancelamento() {
		return (dataCompra, dataEvento) -> {};
	}

	private EstrategiaReembolso estrategiaErro() {
		return (dataCompra, dataEvento) -> {
			if (dataEvento.isBefore(LocalDateTime.now())) {
				throw new ReembolsoNaoPermitidoException("evento já ocorreu: reembolso por erro não aplicável");
			}
		};
	}
}
