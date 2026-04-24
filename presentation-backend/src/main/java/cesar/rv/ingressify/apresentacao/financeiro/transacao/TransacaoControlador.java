package cesar.rv.ingressify.apresentacao.financeiro.transacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.financeiro.transacao.TransacaoResumo;
import cesar.rv.ingressify.aplicacao.financeiro.transacao.TransacaoServicoAplicacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@RestController
@RequestMapping("backend/transacao")
public class TransacaoControlador {

	@Autowired
	private TransacaoServicoAplicacao transacaoServicoAplicacao;

	@GetMapping("historico")
	public List<TransacaoResumo> historico(@RequestParam("usuarioId") int usuarioId) {
		return transacaoServicoAplicacao.pesquisarHistorico(new UsuarioId(usuarioId));
	}
}
