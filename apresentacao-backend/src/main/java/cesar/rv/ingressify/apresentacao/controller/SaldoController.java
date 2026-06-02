package cesar.rv.ingressify.apresentacao.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.financeiro.extrato.ExtratoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.AdicionarSaldoRequest;
import cesar.rv.ingressify.apresentacao.dto.SaldoResponse;
import cesar.rv.ingressify.apresentacao.dto.TransacaoResponse;
import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.ColecaoTransacoes;
import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.TransacaoIterador;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@RestController
public class SaldoController {

	private final ExtratoServicoAplicacao extratoServico;

	public SaldoController(ExtratoServicoAplicacao extratoServico) {
		this.extratoServico = extratoServico;
	}

	@GetMapping("/saldo")
	public ResponseEntity<SaldoResponse> obterSaldo(@RequestHeader("X-Usuario-Id") int usuarioId) {
		var saldo = extratoServico.obterSaldo(new UsuarioId(usuarioId));
		return ResponseEntity.ok(SaldoResponse.fromDomain(saldo));
	}

	@PostMapping("/saldo/adicionar")
	public ResponseEntity<SaldoResponse> adicionar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody AdicionarSaldoRequest req) {
		if (req.valor() == null || req.valor().signum() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		UsuarioId uid = new UsuarioId(usuarioId);
		extratoServico.adicionarSaldo(uid, req.valor());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(SaldoResponse.fromDomain(extratoServico.obterSaldo(uid)));
	}

	@GetMapping("/transacoes")
	public ResponseEntity<List<TransacaoResponse>> listarTransacoes(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(required = false) TipoTransacao tipo) {
		if (page < 1 || limit < 1 || limit > 100) {
			return ResponseEntity.badRequest().build();
		}
		ColecaoTransacoes colecao = extratoServico.colecaoPorUsuario(new UsuarioId(usuarioId));
		TransacaoIterador iter = colecao.criarIterador(page, limit);
		List<TransacaoResponse> resultado = new ArrayList<>();
		while (iter.temProximo()) {
			Transacao t = iter.proximo();
			if (tipo == null || t.getTipo() == tipo) {
				resultado.add(TransacaoResponse.fromDomain(t));
			}
		}
		return ResponseEntity.ok(resultado);
	}

	@GetMapping("/transacoes/{id}")
	public ResponseEntity<TransacaoResponse> detalheTransacao(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@PathVariable int id) {
		ColecaoTransacoes colecao = extratoServico.colecaoPorUsuario(new UsuarioId(usuarioId));
		TransacaoIterador iter = colecao.criarIterador(1, Integer.MAX_VALUE);
		while (iter.temProximo()) {
			Transacao t = iter.proximo();
			if (t.getId() != null && t.getId().getId() == id) {
				return ResponseEntity.ok(TransacaoResponse.fromDomain(t));
			}
		}
		return ResponseEntity.notFound().build();
	}
}
