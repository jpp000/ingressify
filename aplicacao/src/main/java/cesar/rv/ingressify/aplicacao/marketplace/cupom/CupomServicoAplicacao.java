package cesar.rv.ingressify.aplicacao.marketplace.cupom;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.cupom.Cupom;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomServico;
import cesar.rv.ingressify.dominio.marketplace.cupom.TipoCupom;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;

public class CupomServicoAplicacao {

	private final CupomServico cupomServico;
	private final EventoRepositorio eventoRepositorio;

	public CupomServicoAplicacao(CupomServico cupomServico, EventoRepositorio eventoRepositorio) {
		Validate.notNull(cupomServico, "cupomServico");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		this.cupomServico = cupomServico;
		this.eventoRepositorio = eventoRepositorio;
	}

	public Cupom criar(EventoId eventoId, UsuarioId organizadorId, String codigo, TipoCupom tipo, BigDecimal valor,
			BigDecimal valorMinimo, int limiteUsos, LocalDateTime validoDe, LocalDateTime validoAte) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		Dinheiro minimo = valorMinimo != null ? new Dinheiro(valorMinimo) : Dinheiro.ZERO;
		Cupom cupom = new Cupom(codigo, tipo, valor, eventoId, minimo, limiteUsos, validoDe, validoAte);
		return cupomServico.criar(cupom);
	}

	public List<Cupom> listarPorEvento(EventoId eventoId) {
		return cupomServico.listarPorEvento(eventoId);
	}

	/** Pré-visualiza o desconto de um cupom para um valor de compra, sem consumi-lo. */
	public Dinheiro previsualizar(String codigo, EventoId eventoId, BigDecimal valorCompra) {
		return cupomServico.previsualizar(codigo, new Dinheiro(valorCompra), eventoId, LocalDateTime.now());
	}
}
