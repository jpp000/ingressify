package cesar.rv.ingressify.apresentacao.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.identidade.usuario.LoginResultado;
import cesar.rv.ingressify.aplicacao.identidade.usuario.UsuarioServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CadastroRequest;
import cesar.rv.ingressify.apresentacao.dto.LoginRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UsuarioServicoAplicacao usuarioServico;

	public AuthController(UsuarioServicoAplicacao usuarioServico) {
		this.usuarioServico = usuarioServico;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		if (req.email() == null || req.email().isBlank() ||
				req.senha() == null || req.senha().isBlank()) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", "Email e senha são obrigatórios"));
		}
		try {
			LoginResultado resultado = usuarioServico.login(req.email(), req.senha());
			return ResponseEntity.ok(resultado);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(erroResponse("CREDENCIAIS_INVALIDAS", "Email ou senha incorretos"));
		}
	}

	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastro(@RequestBody CadastroRequest req) {
		if (req.nome() == null || req.nome().isBlank()) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", "Nome é obrigatório"));
		}
		if (req.email() == null || req.email().isBlank()) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", "Email é obrigatório"));
		}
		if (req.senha() == null || req.senha().isBlank()) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", "Senha é obrigatória"));
		}
		if (req.papel() == null || req.papel().isBlank()) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", "Papel é obrigatório (COMPRADOR ou ORGANIZADOR)"));
		}
		try {
			LoginResultado resultado = usuarioServico.cadastrarComPapel(
					req.nome(), req.email(), req.senha(), req.papel());
			return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(erroResponse("EMAIL_DUPLICADO", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(erroResponse("DADOS_INVALIDOS", e.getMessage()));
		}
	}

	private Map<String, Object> erroResponse(String codigo, String mensagem) {
		return Map.of("sucesso", false, "erro", Map.of("codigo", codigo, "mensagem", mensagem));
	}
}
