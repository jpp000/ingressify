package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.DecidirDenunciaRequest;
import cesar.rv.ingressify.apresentacao.dto.DenunciaResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DecisaoModeracao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

	private final DenunciaServicoAplicacao denunciaServico;

	public DenunciaController(DenunciaServicoAplicacao denunciaServico) {
		this.denunciaServico = denunciaServico;
	}

	@GetMapping
	public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			List<DenunciaResponse> resp = denunciaServico.listar(new UsuarioId(usuarioId)).stream()
					.map(DenunciaResponse::fromDomain)
					.toList();
			return ResponseEntity.ok(resp);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/{id}/decidir")
	public ResponseEntity<?> decidir(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody DecidirDenunciaRequest req) {
		if (req.decisao() == null || req.decisao().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			denunciaServico.decidir(new DenunciaId(id), DecisaoModeracao.valueOf(req.decisao()),
					new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
