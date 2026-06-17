package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.ReembolsoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;

@RestController
@RequestMapping("/admin/reembolsos")
public class AdminReembolsoController {

	private final ReembolsoServicoAplicacao reembolsoServico;

	public AdminReembolsoController(ReembolsoServicoAplicacao reembolsoServico) {
		this.reembolsoServico = reembolsoServico;
	}

	@GetMapping
	public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			List<ReembolsoResponse> resp = reembolsoServico.listar(new UsuarioId(usuarioId))
					.stream().map(ReembolsoResponse::fromDomain).toList();
			return ResponseEntity.ok(resp);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/{id}/analisar")
	public ResponseEntity<?> analisar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			reembolsoServico.iniciarAnalise(new SolicitacaoReembolsoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/{id}/aprovar")
	public ResponseEntity<?> aprovar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			reembolsoServico.aprovarManual(new SolicitacaoReembolsoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/{id}/recusar")
	public ResponseEntity<?> recusar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			reembolsoServico.recusarManual(new SolicitacaoReembolsoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}
}
