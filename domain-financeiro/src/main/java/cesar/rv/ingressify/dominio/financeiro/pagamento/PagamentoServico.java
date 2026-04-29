package cesar.rv.ingressify.dominio.financeiro.pagamento;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class PagamentoServico {

	private final PagamentoRepositorio repositorio;

	public PagamentoServico(PagamentoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Pagamento criarPendente(Dinheiro valor, UsuarioId comprador, UsuarioId vendedor, TipoOperacao tipo,
			UUID correlacaoId) {
		Pagamento p = new Pagamento(valor, comprador, vendedor, tipo, correlacaoId);
		repositorio.salvar(p);
		return p;
	}

	public Pagamento confirmar(PagamentoId id) {
		Pagamento p = repositorio.obter(id);
		p.confirmar();
		repositorio.salvar(p);
		return p;
	}

	public Pagamento rejeitar(PagamentoId id) {
		Pagamento p = repositorio.obter(id);
		p.rejeitar();
		repositorio.salvar(p);
		return p;
	}

	public Pagamento obter(PagamentoId id) {
		return repositorio.obter(id);
	}

	public Optional<Pagamento> obterPorCorrelacao(UUID correlacaoId) {
		return repositorio.obterPorCorrelacao(correlacaoId);
	}
}
