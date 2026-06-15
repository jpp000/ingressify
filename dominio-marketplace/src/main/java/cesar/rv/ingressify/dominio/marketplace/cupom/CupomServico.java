package cesar.rv.ingressify.dominio.marketplace.cupom;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class CupomServico {

	private final CupomRepositorio repositorio;

	public CupomServico(CupomRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Cupom criar(Cupom cupom) {
		repositorio.buscarPorCodigo(cupom.getCodigo()).ifPresent(c -> {
			throw new IllegalStateException("já existe cupom com este código");
		});
		repositorio.salvar(cupom);
		return cupom;
	}

	public Cupom obterPorCodigo(String codigo) {
		Validate.notBlank(codigo, "codigo");
		return repositorio.buscarPorCodigo(codigo.trim().toUpperCase())
				.orElseThrow(() -> new IllegalStateException("cupom não encontrado"));
	}

	public List<Cupom> listarPorEvento(EventoId eventoId) {
		return repositorio.pesquisarPorEvento(eventoId);
	}

	/** Valida o cupom para a compra e devolve o valor já com desconto, sem consumir um uso. */
	public Dinheiro previsualizar(String codigo, Dinheiro valorCompra, EventoId eventoCompra, LocalDateTime agora) {
		Cupom cupom = obterPorCodigo(codigo);
		cupom.validarAplicavel(valorCompra, eventoCompra, agora);
		return cupom.aplicar(valorCompra);
	}

	/** Valida, registra um uso e devolve o valor com desconto. Usado ao concretizar a compra. */
	public Dinheiro consumir(String codigo, Dinheiro valorCompra, EventoId eventoCompra, LocalDateTime agora) {
		Cupom cupom = obterPorCodigo(codigo);
		cupom.validarAplicavel(valorCompra, eventoCompra, agora);
		Dinheiro comDesconto = cupom.aplicar(valorCompra);
		cupom.registrarUso();
		repositorio.salvar(cupom);
		return comDesconto;
	}
}
