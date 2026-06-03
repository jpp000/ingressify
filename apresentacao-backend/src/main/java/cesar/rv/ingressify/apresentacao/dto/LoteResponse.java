package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.Lote;

public record LoteResponse(
		int id,
		int numero,
		String nome,
		BigDecimal preco,
		int quantidadeTotal,
		int quantidadeDisponivel,
		String dataInicio,
		String dataFim,
		boolean ativo) {

	public static LoteResponse fromDomain(Lote lote) {
		return new LoteResponse(
				lote.getId() != null ? lote.getId().getId() : 0,
				lote.getNumero(),
				lote.getNome(),
				lote.getPreco().getValor(),
				lote.getQuantidadeTotal(),
				lote.getQuantidadeDisponivel(),
				lote.getDataInicio() != null ? lote.getDataInicio().toString() : null,
				lote.getDataFim() != null ? lote.getDataFim().toString() : null,
				lote.ativo(LocalDateTime.now()));
	}
}
