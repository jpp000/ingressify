package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.compra.CompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CompraIngressoRequest;
import cesar.rv.ingressify.apresentacao.dto.IngressoResponse;
import cesar.rv.ingressify.apresentacao.dto.SolicitarReembolsoRequest;
import cesar.rv.ingressify.apresentacao.dto.TransferirIngressoRequest;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.padroes.estrategia.ReembolsoNaoPermitidoException;

@RestController
public class IngressoController {

	private final CompraServicoAplicacao compraServico;
	private final IngressoServicoAplicacao ingressoServico;
	private final ReembolsoServicoAplicacao reembolsoServico;

	public IngressoController(CompraServicoAplicacao compraServico, IngressoServicoAplicacao ingressoServico,
			ReembolsoServicoAplicacao reembolsoServico) {
		this.compraServico = compraServico;
		this.ingressoServico = ingressoServico;
		this.reembolsoServico = reembolsoServico;
	}

	@PostMapping("/ingressos/comprar")
	public ResponseEntity<?> comprar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CompraIngressoRequest req) {
		if (req.tipoIngressoId() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		if (req.quantidade() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		try {
			List<IngressoId> ids = compraServico.comprar(
					new UsuarioId(usuarioId), new TipoIngressoId(req.tipoIngressoId()), req.quantidade());
			List<IngressoResponse> resp = ids.stream()
					.map(id -> IngressoResponse.fromDomain(ingressoServico.obter(id)))
					.toList();
			return ResponseEntity.status(HttpStatus.CREATED).body(resp);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(java.util.Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/meus-ingressos")
	public ResponseEntity<List<IngressoResponse>> meus(
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		List<IngressoResponse> resp = ingressoServico
				.pesquisarPorProprietario(new UsuarioId(usuarioId))
				.stream()
				.map(IngressoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/ingressos/{id}")
	public ResponseEntity<IngressoResponse> detalhe(@PathVariable String id) {
		try {
			UUID uuid = UUID.fromString(id);
			return ResponseEntity.ok(IngressoResponse.fromDomain(ingressoServico.obter(new IngressoId(uuid))));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/ingressos/{id}/transferir")
	public ResponseEntity<Void> transferir(
			@PathVariable String id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody TransferirIngressoRequest req) {
		if (req.emailDestinatario() == null || req.emailDestinatario().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			ingressoServico.transferir(
					new IngressoId(UUID.fromString(id)), new UsuarioId(usuarioId), req.emailDestinatario());
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/ingressos/{id}/reembolsar")
	public ResponseEntity<Void> reembolsar(
			@PathVariable String id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			reembolsoServico.solicitar(new IngressoId(UUID.fromString(id)), new UsuarioId(usuarioId));
			return ResponseEntity.accepted().build();
		} catch (ReembolsoNaoPermitidoException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/ingressos/{id}/qrcode")
	public ResponseEntity<String> qrcode(@PathVariable String id) {
		try {
			UUID uuid = UUID.fromString(id);
			ingressoServico.obter(new IngressoId(uuid));
			return ResponseEntity.ok()
					.header("Content-Type", "text/plain")
					.body(id);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
