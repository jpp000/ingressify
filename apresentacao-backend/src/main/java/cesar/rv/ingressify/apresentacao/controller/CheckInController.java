package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.checkin.CheckinServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.BuscaManualRequest;
import cesar.rv.ingressify.apresentacao.dto.CheckInItemResponse;
import cesar.rv.ingressify.apresentacao.dto.EscanearQRRequest;
import cesar.rv.ingressify.apresentacao.dto.ResultadoCheckInResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@RestController
@RequestMapping("/check-in")
public class CheckInController {

	private final CheckinServicoAplicacao checkinServico;

	public CheckInController(CheckinServicoAplicacao checkinServico) {
		this.checkinServico = checkinServico;
	}

	@PostMapping("/escanear")
	public ResponseEntity<ResultadoCheckInResponse> escanear(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody EscanearQRRequest req) {
		if (req.qrCode() == null || req.qrCode().isBlank()) {
			return ResponseEntity.badRequest()
					.body(new ResultadoCheckInResponse(false, "QR Code não informado", null, null));
		}
		try {
			RegistroCheckin registro = checkinServico.realizarCheckin(req.qrCode(), new UsuarioId(usuarioId));
			return ResponseEntity.ok(ResultadoCheckInResponse.fromDomain(registro));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest()
					.body(new ResultadoCheckInResponse(false, e.getMessage(), null, null));
		}
	}

	@PostMapping("/manual")
	public ResponseEntity<ResultadoCheckInResponse> manual(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody BuscaManualRequest req) {
		if (req.codigoIngresso() == null || req.codigoIngresso().isBlank()) {
			return ResponseEntity.badRequest()
					.body(new ResultadoCheckInResponse(false, "Código do ingresso não informado", null, null));
		}
		try {
			IngressoId ingressoId = checkinServico.buscarIngressoPorCodigo(req.codigoIngresso());
			RegistroCheckin registro = checkinServico.realizarCheckin(
					ingressoId.getId().toString(), new UsuarioId(usuarioId));
			return ResponseEntity.ok(ResultadoCheckInResponse.fromDomain(registro));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest()
					.body(new ResultadoCheckInResponse(false, e.getMessage(), null, null));
		}
	}

	@GetMapping("/relatorio/{eventoId}")
	public ResponseEntity<List<CheckInItemResponse>> relatorio(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		List<CheckInItemResponse> resp = checkinServico
				.relatorio(new EventoId(eventoId))
				.stream()
				.map(CheckInItemResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}
}
