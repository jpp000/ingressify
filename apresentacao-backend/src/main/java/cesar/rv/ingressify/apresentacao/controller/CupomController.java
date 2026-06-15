package cesar.rv.ingressify.apresentacao.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.cupom.CupomServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarCupomRequest;
import cesar.rv.ingressify.apresentacao.dto.CupomResponse;
import cesar.rv.ingressify.apresentacao.dto.ValidarCupomRequest;
import cesar.rv.ingressify.apresentacao.dto.ValidarCupomResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
public class CupomController {

	private final CupomServicoAplicacao cupomServico;

	public CupomController(CupomServicoAplicacao cupomServico) {
		this.cupomServico = cupomServico;
	}

	@PostMapping("/eventos/{eventoId}/cupons")
	public ResponseEntity<?> criar(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarCupomRequest req) {
		if (req.codigo() == null || req.codigo().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Código do cupom é obrigatório."));
		}
		if (req.tipo() == null || req.valor() == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Tipo e valor do cupom são obrigatórios."));
		}
		try {
			var cupom = cupomServico.criar(
					new EventoId(eventoId), new UsuarioId(usuarioId),
					req.codigo(), req.tipo(), req.valor(), req.valorMinimo(),
					req.limiteUsos(), req.validoDe(), req.validoAte());
			return ResponseEntity.status(HttpStatus.CREATED).body(CupomResponse.fromDomain(cupom));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping("/eventos/{eventoId}/cupons")
	public ResponseEntity<List<CupomResponse>> listar(@PathVariable int eventoId) {
		List<CupomResponse> lista = cupomServico.listarPorEvento(new EventoId(eventoId))
				.stream().map(CupomResponse::fromDomain).toList();
		return ResponseEntity.ok(lista);
	}

	@PostMapping("/cupons/validar")
	public ResponseEntity<?> validar(@RequestBody ValidarCupomRequest req) {
		if (req.codigo() == null || req.codigo().isBlank() || req.valor() == null || req.valor().signum() <= 0) {
			return ResponseEntity.badRequest().body(Map.of("message", "Código e valor são obrigatórios."));
		}
		try {
			BigDecimal comDesconto = cupomServico
					.previsualizar(req.codigo(), new EventoId(req.eventoId()), req.valor())
					.getValor();
			BigDecimal desconto = req.valor().subtract(comDesconto);
			return ResponseEntity.ok(new ValidarCupomResponse(
					req.codigo().trim().toUpperCase(), req.valor(), desconto, comDesconto));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
}
