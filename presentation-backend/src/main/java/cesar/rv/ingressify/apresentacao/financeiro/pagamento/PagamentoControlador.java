package cesar.rv.ingressify.apresentacao.financeiro.pagamento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.pagamento.ConfirmarPagamentoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;

@RestController
@RequestMapping("backend/pagamento")
public class PagamentoControlador {

	@Autowired
	private ConfirmarPagamentoServicoAplicacao confirmarPagamentoServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@PostMapping("{id}/confirmar")
	public void confirmar(@PathVariable("id") int id) {
		confirmarPagamentoServicoAplicacao.confirmar(mapeador.paraPagamentoId(id));
	}

	@PostMapping("{id}/rejeitar")
	public void rejeitar(@PathVariable("id") int id) {
		confirmarPagamentoServicoAplicacao.rejeitar(mapeador.paraPagamentoId(id));
	}
}
