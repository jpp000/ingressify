package cesar.rv.ingressify.apresentacao.financeiro.saldo;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.financeiro.saldo.SaldoResumo;
import cesar.rv.ingressify.aplicacao.financeiro.saldo.SaldoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.saldo.AjustarSaldoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@RestController
@RequestMapping("backend/saldo")
public class SaldoControlador {

	@Autowired
	private SaldoServicoAplicacao saldoServicoAplicacao;

	@Autowired
	private AjustarSaldoServicoAplicacao ajustarSaldoServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@GetMapping("{usuarioId}")
	public SaldoResumo obter(@PathVariable("usuarioId") int usuarioId) {
		return saldoServicoAplicacao.obterResumo(new UsuarioId(usuarioId));
	}

	@PutMapping("{usuarioId}")
	public void ajustar(@PathVariable("usuarioId") int usuarioId, @RequestBody BigDecimal novoValor) {
		ajustarSaldoServicoAplicacao.ajustar(new UsuarioId(usuarioId), mapeador.paraDinheiro(novoValor));
	}
}
