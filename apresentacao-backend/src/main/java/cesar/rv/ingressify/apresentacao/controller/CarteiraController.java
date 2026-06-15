package cesar.rv.ingressify.apresentacao.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.financeiro.carteira.CarteiraServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.RecargaRequest;
import cesar.rv.ingressify.apresentacao.dto.SaldoResponse;
import cesar.rv.ingressify.apresentacao.dto.SaqueRequest;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@RestController
@RequestMapping("/carteira")
public class CarteiraController {

	private final CarteiraServicoAplicacao carteiraServico;

	public CarteiraController(CarteiraServicoAplicacao carteiraServico) {
		this.carteiraServico = carteiraServico;
	}

	@GetMapping
	public ResponseEntity<SaldoResponse> obter(@RequestHeader("X-Usuario-Id") int usuarioId) {
		return ResponseEntity.ok(SaldoResponse.fromDomain(carteiraServico.obterSaldo(new UsuarioId(usuarioId))));
	}

	@PostMapping("/recarregar")
	public ResponseEntity<?> recarregar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody RecargaRequest req) {
		if (req.valor() == null || req.valor().signum() <= 0) {
			return ResponseEntity.badRequest().body(Map.of("motivo", "valor deve ser positivo"));
		}
		var saldo = carteiraServico.recarregar(new UsuarioId(usuarioId), req.valor());
		return ResponseEntity.status(HttpStatus.CREATED).body(SaldoResponse.fromDomain(saldo));
	}

	@PostMapping("/sacar")
	public ResponseEntity<?> sacar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody SaqueRequest req) {
		if (req.valor() == null || req.valor().signum() <= 0) {
			return ResponseEntity.badRequest().body(Map.of("motivo", "valor deve ser positivo"));
		}
		try {
			var saldo = carteiraServico.sacar(new UsuarioId(usuarioId), req.valor());
			return ResponseEntity.ok(SaldoResponse.fromDomain(saldo));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
		}
	}
}
