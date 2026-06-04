package cesar.rv.ingressify.apresentacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.analytics.AnalyticsServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.analytics.EventoAnalyticsDto;
import cesar.rv.ingressify.apresentacao.dto.EventoAnalyticsResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
@RequestMapping("/eventos/{eventoId}/analytics")
public class AnalyticsController {

	private final AnalyticsServicoAplicacao analyticsServico;

	public AnalyticsController(AnalyticsServicoAplicacao analyticsServico) {
		this.analyticsServico = analyticsServico;
	}

	@GetMapping
	public ResponseEntity<EventoAnalyticsResponse> obter(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			EventoAnalyticsDto dto = analyticsServico.calcular(new EventoId(eventoId), new UsuarioId(usuarioId));
			return ResponseEntity.ok(new EventoAnalyticsResponse(
					dto.totalVendidos(), dto.totalRevendidos(), dto.totalDisponiveis(),
					dto.totalCapacidade(), dto.taxaOcupacao(), dto.taxaRevenda(),
					dto.mediaAvaliacao(), dto.totalAvaliacoes()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(403).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
